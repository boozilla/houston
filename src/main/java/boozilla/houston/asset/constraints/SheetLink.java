package boozilla.houston.asset.constraints;

import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.AssetLink;
import boozilla.houston.asset.sql.Select;
import boozilla.houston.exception.AssetTypeMismatchException;
import boozilla.houston.exception.AssetVerifyException;
import boozilla.houston.utils.MessageUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SheetLink extends LocalizedAssetSheetConstraints {
    @Override
    public Optional<String> targetSheetName()
    {
        return Optional.empty();
    }

    @Override
    public Flux<Throwable> check(final PrintWriter writer, final AssetAccessor accessor)
    {
        final var updated = accessor.updatedMergeKey();

        return Flux.fromIterable(updated)
                .flatMap(key -> accessor.links(key.sheetName())
                        .flatMap(link -> typeChecking(link, accessor)
                                .switchIfEmpty(nonExists(link, accessor))));
    }

    @Override
    public String subject()
    {
        return message("CONSTRAINTS_SUBJECT_SHEET_LINK");
    }

    private Flux<Throwable> typeChecking(final AssetLink link, final AssetAccessor accessor)
    {
        final var target = link.getRelated();

        final var linkType = accessor.columnType(link.getSheetName(), link.getColumnName());
        final var targetType = accessor.columnType(target.getSheetName(), "code");

        if(!linkType.equals(targetType))
        {
            return Flux.just(new AssetTypeMismatchException(link.getSheetName(), link.getColumnName(), linkType,
                    target.getSheetName(), target.getColumnName(), targetType));
        }

        return Flux.empty();
    }

    private Flux<Throwable> nonExists(final AssetLink link, final AssetAccessor accessor)
    {
        final var target = link.getRelated();

        // 1단계: 링크된 값들 수집
        return collectLinkedValues(link, accessor)
                // 2단계: 대상 시트에 존재하지 않는 값들 찾기
                .flatMapMany(linkedValues -> findNonExistValues(linkedValues, target, accessor))
                // 3단계: 존재하지 않는 값에 대한 오류 생성
                .flatMap(nonExistValues -> createErrorsForNonExistValues(nonExistValues, link, accessor));
    }

    private Mono<Set<Object>> collectLinkedValues(final AssetLink link, final AssetAccessor accessor)
    {
        final var query = Select.columns(link.getColumnName())
                .from(link.getSheetName());

        return accessor.query(link, query)
                .flatMap(data -> Flux.fromStream(data.stream(link.getColumnName(), Object.class))
                        .flatMap(row -> MessageUtils.extractValue(row)
                                .map(Flux::just)
                                .orElse(Flux.empty())))
                .collect(Collectors.toUnmodifiableSet())
                .filter(targetRows -> !targetRows.isEmpty());
    }

    private Mono<Set<Object>> findNonExistValues(final Set<Object> linkedValues,
                                                 final AssetLink target,
                                                 final AssetAccessor accessor)
    {
        final var primaryColumn = "code";
        final var expression = target.getExpression();

        // 1. 존재하지 않는 값 검증
        final var existsQuery = Select.columns(primaryColumn)
                .from(target.getSheetName())
                .where(":COLUMN IN :VALUES")
                .parameter("COLUMN", primaryColumn)
                .parameter("VALUES", linkedValues);

        final var nonExistValuesMono = accessor.query(target, existsQuery)
                .flatMap(data -> Flux.fromStream(data.stream(primaryColumn, Object.class)))
                .collect(Collectors.toUnmodifiableSet())
                .map(existValues -> linkedValues.stream()
                        .filter(value -> !existValues.contains(value))
                        .collect(Collectors.toUnmodifiableSet()));

        // 표현식이 없는 경우 존재하지 않는 값만 반환
        if(Objects.isNull(expression))
        {
            return nonExistValuesMono;
        }

        // 2. 표현식에서 허용된 값들을 추출
        final var allowValues = extractAllowedValuesFromExpression(expression);

        // 허용된 값들을 사용하여 쿼리 실행
        final var passedExpressionQuery = Select.columns("code")
                .from(target.getSheetName())
                .where("code IN :CODE_VALUES AND :TARGET IN :ALLOW_VALUES")
                .parameter("CODE_VALUES", linkedValues)
                .parameter("TARGET", target.getColumnName())
                .parameter("ALLOW_VALUES", allowValues);

        final var expressionInvalidValuesMono = accessor.query(target, passedExpressionQuery)
                .flatMap(data -> Flux.fromStream(data.stream("code", Object.class)))
                .collect(Collectors.toUnmodifiableSet())
                .map(passedValues -> linkedValues.stream()
                        .filter(value -> !passedValues.contains(value))
                        .collect(Collectors.toUnmodifiableSet()));

        // 3. 두 결과를 합침
        return Mono.zip(nonExistValuesMono, expressionInvalidValuesMono)
                .map(tuple -> {
                    final var nonExistValues = tuple.getT1();
                    final var expressionInvalidValues = tuple.getT2();

                    return Stream.concat(nonExistValues.stream(), expressionInvalidValues.stream())
                            .collect(Collectors.toUnmodifiableSet());
                });
    }

    private Flux<Throwable> createErrorsForNonExistValues(final Set<Object> nonExistValues,
                                                          final AssetLink link,
                                                          final AssetAccessor accessor)
    {
        if(nonExistValues.isEmpty())
        {
            return Flux.empty();
        }

        final var query = Select.columns(link.getColumnName())
                .from(link.getSheetName())
                .where(":COLUMN IN :VALUES")
                .parameter("COLUMN", link.getColumnName())
                .parameter("VALUES", nonExistValues);

        return accessor.query(link, query)
                .flatMap(data -> createExceptionFromData(data, link));
    }

    private Flux<Throwable> createExceptionFromData(final AssetData data, final AssetLink link)
    {
        final var value = data.value(link.getColumnName(), Object.class);
        final var partition = data.value("partition", String.class);
        final var sheetName = Stream.of(link.getSheetName(), partition)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("#"));

        final var valueStream = createValueStream(value);

        return Flux.fromStream(valueStream)
                .map(v -> new AssetVerifyException(
                        message("CONSTRAINTS_ERROR_LINKED"),
                        sheetName,
                        link.getColumnName(),
                        v));
    }

    private Stream<?> createValueStream(final Object value)
    {
        if(value instanceof final Collection<?> collection)
        {
            return collection.stream();
        }
        else
        {
            return Stream.of(value);
        }
    }

    /**
     * 표현식에서 내용을 추출합니다.
     * 유효하지 않은 형식이면 빈 문자열을 반환합니다.
     *
     * @param expression 표현식
     * @return 표현식 내용 ([] 제외)
     */
    private String extractExpressionContent(final String expression)
    {
        // 표현식 형식 검증
        if(!isValidExpressionFormat(expression))
        {
            return "";
        }

        // 표현식에서 [] 안의 내용 추출
        return expression.substring(1, expression.length() - 1).trim();
    }

    /**
     * 표현식에서 허용된 값들을 추출합니다.
     * <p>
     * 지원하는 표현식 형식:
     * [0] = 0 값만 허용됩니다.
     * [1] = 1 값만 허용됩니다.
     * [0, 1] = 0 또는 1 값이 허용됩니다.
     * ["One", "Two", "Three"] = One, Two, Three 값이 허용됩니다.
     * [0:100] = 0 ~ 100 사이 값이 허용됩니다.
     *
     * @param expression 검증 표현식
     * @return 허용된 값들의 집합
     */
    private Set<Object> extractAllowedValuesFromExpression(final String expression)
    {
        // 표현식 내용 추출
        final var content = extractExpressionContent(expression);

        // 표현식이 유효하지 않거나 빈 경우
        if(content.isEmpty())
        {
            return Set.of();
        }

        // 표현식 유형에 따라 처리
        if(isRangeExpression(content))
        {
            return extractAllowedValuesFromRangeExpression(content);
        }
        else
        {
            return extractAllowedValuesFromListExpression(content);
        }
    }

    /**
     * 범위 표현식에서 허용된 값들을 추출합니다.
     *
     * @param content 표현식 내용 ([] 제외)
     * @return 허용된 값들의 집합
     */
    private Set<Object> extractAllowedValuesFromRangeExpression(final String content)
    {
        return parseRangeExpression(content).allowedValues();
    }

    /**
     * 범위 표현식을 파싱하여 범위 정보를 반환합니다.
     *
     * @param content 표현식 내용 ([] 제외)
     * @return 범위 정보 (최소값, 최대값, 허용된 값들의 집합)
     */
    private RangeInfo parseRangeExpression(final String content)
    {
        final var parts = content.split(":");

        if(parts.length != 2)
        {
            return new RangeInfo(null, null, Set.of()); // 잘못된 범위 형식
        }

        try
        {
            final var allowedValues = new HashSet<>();
            final var min = Double.parseDouble(parts[0].trim());
            final var max = Double.parseDouble(parts[1].trim());

            // 정수 범위인 경우 정수 값들을 생성
            if(min == Math.floor(min) && max == Math.floor(max))
            {
                for(long i = (long) min; i <= (long) max; i++)
                {
                    allowedValues.add(i);
                }
            }
            else
            {
                // 실수 범위인 경우 몇 개의 샘플 값을 생성
                allowedValues.add(min);
                allowedValues.add(max);
                allowedValues.add((min + max) / 2);
            }

            return new RangeInfo(min, max, allowedValues);
        }
        catch(NumberFormatException e)
        {
            return new RangeInfo(null, null, Set.of()); // 숫자 변환 실패
        }
    }

    /**
     * 문자열 목록 표현식에서 허용된 값들을 추출합니다.
     *
     * @param content 표현식 내용 ([] 제외)
     * @return 허용된 값들의 집합
     */
    private Set<Object> extractAllowedValuesFromStringListExpression(final String content)
    {
        // 빈 표현식이면 빈 집합 반환
        if(content.isEmpty())
        {
            return Set.of();
        }

        return parseStringListExpression(content);
    }

    /**
     * 목록 표현식에서 허용된 값들을 추출합니다.
     *
     * @param content 표현식 내용 ([] 제외)
     * @return 허용된 값들의 집합
     */
    private Set<Object> extractAllowedValuesFromListExpression(final String content)
    {
        // 빈 표현식이면 빈 집합 반환
        if(content.isEmpty())
        {
            return Set.of();
        }

        // 따옴표가 포함되어 있으면 문자열 목록으로 처리
        final var isStringList = content.contains("\"") || content.contains("'");

        if(isStringList)
        {
            return extractAllowedValuesFromStringListExpression(content);
        }
        else
        {
            return extractAllowedValuesFromNumericListExpression(content);
        }
    }

    /**
     * 문자열 목록 표현식을 파싱하여 값들의 집합을 반환합니다.
     *
     * @param content 표현식 내용 ([] 제외)
     * @return 파싱된 값들의 집합
     */
    private Set<Object> parseStringListExpression(final String content)
    {
        final var values = new HashSet<>();

        // 문자열 목록에서 따옴표와 공백 제거 후 분리
        final var cleanContent = content.replaceAll("[\"']", "").trim();
        final var items = cleanContent.split(",\\s*");

        // 표현식에서 값들을 추출
        for(final var item : items)
        {
            final var trimmedItem = item.trim();

            if(!trimmedItem.isEmpty())
            {
                values.add(trimmedItem);
            }
        }

        return values;
    }

    /**
     * 숫자 목록 표현식에서 허용된 값들을 추출합니다.
     *
     * @param content 표현식 내용 ([] 제외)
     * @return 허용된 값들의 집합
     */
    private Set<Object> extractAllowedValuesFromNumericListExpression(final String content)
    {
        // 빈 표현식이면 빈 집합 반환
        if(content.isEmpty())
        {
            return Set.of();
        }

        return parseNumericListExpression(content);
    }

    /**
     * 숫자 목록 표현식을 파싱하여 값들의 집합을 반환합니다.
     *
     * @param content 표현식 내용 ([] 제외)
     * @return 파싱된 값들의 집합
     */
    private Set<Object> parseNumericListExpression(final String content)
    {
        final var values = new HashSet<>();

        // 표현식에서 값들을 추출
        final var items = content.split(",\\s*");

        for(final var item : items)
        {
            final var trimmedItem = item.trim();

            if(trimmedItem.isEmpty())
            {
                continue;
            }

            try
            {
                // 정수인지 실수인지 확인하여 적절한 타입으로 변환
                if(trimmedItem.contains("."))
                {
                    values.add(Double.parseDouble(trimmedItem));
                }
                else
                {
                    values.add(Long.parseLong(trimmedItem));
                }
            }
            catch(NumberFormatException e)
            {
                // 숫자 변환 실패 시 문자열로 처리
                values.add(trimmedItem);
            }
        }

        return values;
    }

    /**
     * 범위 표현식에 대한 값 검증을 수행합니다.
     *
     * @param linkedValues 검증할 값들
     * @param content      표현식 내용
     * @return 유효하지 않은 값들의 집합
     */
    private Set<Object> validateRangeExpression(final Set<Object> linkedValues, final String content)
    {
        final var rangeInfo = parseRangeExpression(content);

        // 범위 정보가 유효하지 않은 경우 모든 값을 유효하지 않다고 간주
        if(rangeInfo.min() == null || rangeInfo.max() == null)
        {
            return Set.copyOf(linkedValues);
        }

        // 숫자가 아닌 값들은 먼저 필터링
        final var nonNumericValues = linkedValues.stream()
                .filter(value -> !(value instanceof Number))
                .collect(Collectors.toSet());

        // 숫자 값들만 처리
        final var numericValues = linkedValues.stream()
                .filter(value -> value instanceof Number)
                .collect(Collectors.toSet());

        if(numericValues.isEmpty())
        {
            return nonNumericValues; // 숫자 값이 없으면 숫자가 아닌 값들만 반환
        }

        // 범위를 벗어난 숫자 값들을 찾음
        final var invalidNumericValues = numericValues.stream()
                .filter(value -> !rangeInfo.isInRange((Number) value))
                .collect(Collectors.toSet());

        // 숫자가 아닌 값들과 범위를 벗어난 숫자 값들을 합침
        final var allInvalidValues = new HashSet<>(nonNumericValues);
        allInvalidValues.addAll(invalidNumericValues);

        return allInvalidValues;
    }

    /**
     * 표현식이 유효한 형식인지 확인합니다.
     * 유효한 표현식은 '[' 로 시작하고 ']' 로 끝나야 합니다.
     *
     * @param expression 검증할 표현식
     * @return 유효한 형식이면 true, 아니면 false
     */
    private boolean isValidExpressionFormat(final String expression)
    {
        // null 체크
        if(expression == null)
        {
            return false;
        }

        // 최소 길이 체크 ([] 최소 2글자)
        if(expression.length() < 2)
        {
            return false;
        }

        // 시작과 끝 문자 체크
        return expression.startsWith("[") && expression.endsWith("]");
    }

    /**
     * 표현식이 범위 표현식인지 확인합니다.
     * 범위 표현식은 ':' 문자를 포함하고 있어야 합니다. (예: "0:100")
     *
     * @param content 표현식 내용
     * @return 범위 표현식이면 true, 아니면 false
     */
    private boolean isRangeExpression(final String content)
    {
        // null 체크
        if(content == null)
        {
            return false;
        }

        // 콜론(:) 포함 여부 확인
        return content.contains(":");
    }

    /**
     * 문자열 목록 표현식에 대한 값 검증을 수행합니다.
     * 표현식에 포함된 문자열 값들과 일치하지 않는 값들을 찾아 반환합니다.
     *
     * @param linkedValues 검증할 값들
     * @param content      표현식 내용
     * @return 유효하지 않은 값들의 집합
     */
    private Set<Object> validateStringListExpression(final Set<Object> linkedValues, final String content)
    {
        // 빈 표현식이면 모든 값이 유효하지 않음
        if(content.isEmpty())
        {
            return Set.copyOf(linkedValues);
        }

        final var allowedValues = parseStringListExpression(content);
        final var invalidValues = new HashSet<>();

        // 허용된 값들에 포함되지 않는 값들을 찾음
        for(final var value : linkedValues)
        {
            final var strValue = value.toString();

            if(!allowedValues.contains(strValue))
            {
                invalidValues.add(value);
            }
        }

        return invalidValues;
    }

    /**
     * 목록 표현식에 대한 값 검증을 수행합니다.
     * 문자열 목록인지 숫자 목록인지 판단하여 적절한 검증 메서드를 호출합니다.
     *
     * @param linkedValues 검증할 값들
     * @param content      표현식 내용
     * @return 유효하지 않은 값들의 집합
     */
    private Set<Object> validateListExpression(final Set<Object> linkedValues, final String content)
    {
        // 빈 표현식이면 모든 값이 유효하지 않음
        if(content.isEmpty())
        {
            return linkedValues;
        }

        // 따옴표가 포함되어 있으면 문자열 목록으로 처리
        final var isStringList = content.contains("\"") || content.contains("'");

        if(isStringList)
        {
            return validateStringListExpression(linkedValues, content);
        }
        else
        {
            return validateNumericListExpression(linkedValues, content);
        }
    }

    /**
     * 숫자 목록 표현식에 대한 값 검증을 수행합니다.
     * 표현식에 포함된 숫자 값들과 일치하지 않는 값들을 찾아 반환합니다.
     *
     * @param linkedValues 검증할 값들
     * @param content      표현식 내용
     * @return 유효하지 않은 값들의 집합
     */
    private Set<Object> validateNumericListExpression(final Set<Object> linkedValues, final String content)
    {
        // 빈 표현식이면 모든 값이 유효하지 않음
        if(content.isEmpty())
        {
            return linkedValues;
        }

        final var allowedValues = parseNumericListExpression(content);
        final var invalidValues = new HashSet<>();

        // 허용된 값들에 포함되지 않는 값들을 찾음
        for(final var value : linkedValues)
        {
            if(!allowedValues.contains(value))
            {
                invalidValues.add(value);
            }
        }

        return invalidValues;
    }

    /**
     * 범위 정보를 저장하는 레코드 클래스
     */
    private record RangeInfo(Double min, Double max, Set<Object> allowedValues) {
        /**
         * 주어진 값이 범위 내에 있는지 확인합니다.
         *
         * @param value 확인할 값
         * @return 범위 내에 있으면 true, 아니면 false
         */
        public boolean isInRange(final Number value)
        {
            if(min == null || max == null)
            {
                return false;
            }

            final var doubleValue = value.doubleValue();
            return doubleValue >= min && doubleValue <= max;
        }
    }
}
