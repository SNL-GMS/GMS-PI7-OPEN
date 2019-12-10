package gms.shared.frameworks.client;

/**
 * Convenience interface that combines request and response content protocol interfaces.
 * @param <W> the type on the wire, e.g. String or byte[]
 */
interface ContentProtocol<W>
    extends RequestContentProtocol<W>, ResponseContentProtocol<W> {

}

