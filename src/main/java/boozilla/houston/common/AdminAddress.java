package boozilla.houston.common;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.util.List;
import java.util.function.Predicate;

public record AdminAddress(
        List<Predicate<InetAddress>> predicates
) {
    public boolean is(final String address)
    {
        return is(InetAddresses.forString(address));
    }

    public boolean is(final InetAddress address)
    {
        return predicates.stream().
                anyMatch(p -> p.test(address));
    }
}
