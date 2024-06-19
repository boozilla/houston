package boozilla.houston.asset.converter;

import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class XmlConverter implements AutoCloseable {
    private static final String NS_SPREAD_SHEET = "urn:schemas-microsoft-com:office:spreadsheet";

    private final Document document;
    private final Workbook workbook;
    private final ByteArrayOutputStream outputStream;

    private XmlConverter(final InputStream in) throws IOException, SAXException,
                                                      ParserConfigurationException
    {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        final var builder = factory.newDocumentBuilder();

        document = builder.parse(in);
        document.getDocumentElement().normalize();

        outputStream = new ByteArrayOutputStream();
        workbook = new Workbook(outputStream, "Excel", version());
    }

    public static Mono<byte[]> toByteArray(final InputStream xmlInputStream)
    {
        return Mono.usingWhen(
                Mono.fromCallable(() -> new XmlConverter(xmlInputStream)),
                converter -> Flux.fromStream(elementStream(converter.document.getElementsByTagName("Worksheet")))
                        .doOnNext(worksheetNode -> {
                            final var sheetName = worksheetNode.getAttributeNS(NS_SPREAD_SHEET, "Name");
                            final var worksheet = converter.workbook.newWorksheet(sheetName);
                            converter.fillData(worksheet, elementStream(worksheetNode.getElementsByTagName("Row")));
                        })
                        .then()
                        .then(Mono.fromCallable(() -> {
                            converter.workbook.finish();
                            return converter.outputStream.toByteArray();
                        })),
                XmlConverter::closeAsync
        ).onErrorStop();
    }

    private static Stream<Element> elementStream(final NodeList nodeList)
    {
        return IntStream.range(0, nodeList.getLength())
                .filter(i -> nodeList.item(i).getNodeType() == Node.ELEMENT_NODE)
                .mapToObj(i -> (Element) nodeList.item(i));
    }

    private void fillData(final Worksheet worksheet, final Stream<Element> rowStream)
    {
        final var rowIndex = new AtomicInteger();

        rowStream.forEach(row -> {
            final var c = new AtomicInteger();
            final var cellStream = elementStream(row.getElementsByTagName("Cell"));

            cellStream.forEach(cell -> {
                final var index = cell.getAttributeNS(NS_SPREAD_SHEET, "Index");
                if(!index.isEmpty())
                {
                    c.set(Integer.parseInt(index) - 1);
                }

                final var optData = Optional.ofNullable((Element) cell.getElementsByTagName("Data").item(0));
                final var data = optData.orElseGet(() -> (Element) cell.getElementsByTagNameNS(NS_SPREAD_SHEET, "Data").item(0));
                final var columnIndex = c.getAndIncrement();

                if(Objects.nonNull(data))
                {
                    worksheet.value(rowIndex.get(), columnIndex, getContent(data));
                }
            });

            rowIndex.incrementAndGet();
        });
    }

    private String getContent(final Element data)
    {
        final var type = data.getAttributeNS(NS_SPREAD_SHEET, "Type");

        if(!type.isEmpty() && type.contentEquals("Boolean"))
        {
            return data.getTextContent().contentEquals("1") ? "true" : "false";
        }

        return data.getTextContent();
    }

    private String version()
    {
        final var properties = elementStream(document.getElementsByTagName("DocumentProperties"));
        final var versionStream = elementStream(properties.findAny()
                .orElseThrow()
                .getElementsByTagName("Version"));
        final var version = versionStream.limit(1)
                .findAny()
                .orElseThrow();

        return version.getTextContent();
    }

    @Override
    public void close() throws Exception
    {
        if(Objects.nonNull(workbook))
            workbook.close();

        if(Objects.nonNull(outputStream))
            outputStream.close();
    }

    private static Mono<Void> closeAsync(XmlConverter converter)
    {
        return Mono.fromRunnable(() -> {
            try
            {
                converter.close();
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        });
    }
}
