import Axios, { AxiosInstance } from 'axios';
import * as msgpack from 'msgpack-lite';
import { clone, toLower } from 'lodash';
import { gatewayLogger as logger } from '../log/gateway-logger';

/**
 * A lightweight wrapper around the Axios mock adapter library that provides convenience
 * wrapper functions to register mock handlers around simulated gms backend services.
 */
export class HttpMockWrapper {

    /** Mocks */
    private mocks: Map<string, (input: any) => Promise<any>> = new Map();

    /**
     * Initialize the axios mock wrapper with an instance of the axios mock adapter.
     * @param mockAdapter The axios mock adapter to wrap
     */
    public constructor() {
        this.mocks = new Map();
    }

    /**
     * Checks if mock url exists
     * @param url url
     * @returns a boolean
     */
    public has(url: string) {
        return this.mocks.has(url);
    }

    /**
     * Removes the mock handles
     * @param url url
     */
    public remove(url: string) {
        logger.debug(`Removing mock handers : ${url}`);
        this.mocks.delete(url);
    }

    /**
     * Clears the mock handles
     */
    public clear(): void {
        logger.debug(`Removing all mock handers.`);
        this.mocks.clear();
    }

    /**
     * Configure a mock service interface at the provided URL, with the provided handler function. 
     * @param url The URL to mock a POST service interface for
     * @param handler The handler function to be called whenever a request is sent to the input URL.
     * This function should accept an input object representing the parsed JSON request body, and should
     * return an object representing the result to encode as the HTTP response body.
     */
    public onMock(url: string, handler: (input?: any) => any): void {
        logger.info(`Registering mock handler for url ${url}`);
        this.mocks.set(url, async (input?: any): Promise<any> =>
            // tslint:disable-next-line: no-inferred-empty-object-type
            new Promise((resolve: any) => {
                resolve(handler(input));
            })
            .catch(error => {
                logger.error(`Error in mock response url: ${url}  input: ${JSON.stringify(input)}`);
                logger.error(`Error in mock response: ${error}`);
            })
        );
    }

    /**
     * Http request helper method
     * @param requestConfig request config
     * @param data request body
     * @returns request response as promise
     */
    public async request(requestConfig: any, data?: any): Promise<any> {
        if (this.has(requestConfig)) {
            const result = this.mocks.get(requestConfig);
            const methodreturn = result(data);
            return methodreturn;
        }
    }
}

/**
 * A lightweight wrapper around the Axios HTTP client providing convenience functions for
 * making HTTP GET and POST requests.
 */
export class HttpClientWrapper {

    /** The axios HTTP client instance */
    private axiosInstance: AxiosInstance;

    /** Axios mock http wrapper */
    private mockAdaptor: HttpMockWrapper;

    /**
     * Initialize the axios wrapper with an instance of the axios http client.
     * @param config (optional) configuration settings used to intialize the axios client
     */
    public constructor(config?: any) {
        // TODO consider passing config parameters to the axios.create() method (e.g. base url)
        // tslint:disable-next-line:no-magic-numbers
        this.axiosInstance = Axios.create({maxContentLength: 100 * 1024 * 1024});
    }

    /**
     * Handles real and mock http request
     * @param requestConfig request config
     * @param data request body data
     * @returns request result as promise
     */
    public async request(requestConfig: any, data?: any): Promise<any> {
        // Throw an error if the request configuration is undefined
        if (!requestConfig) {
            throw new Error('Cannot send HTTP service request with undefined request configuration');
        }
        // Throw an error if the request configuration does not include the url
        if (!requestConfig.url) {
            throw new Error('Cannot send HTTP service request with undefined URL');
        }

        if (this.mockAdaptor && this.mockAdaptor.has(requestConfig.url)) {
            return this.mockAdaptor.request(requestConfig.url, data);
        }

        // Build the HTTP request object from the provided request config and data
        const request = clone(requestConfig);
        // If request data are provided, provide them as 'params' for GET requests or 'data'
        // for all other methods
        if (data) {
            // check if a post or a get
            if  (!request.method || toLower(request.method) === 'get') {
                const paramsSerializer = params => params ?
                    Object.keys(params).map(key => `${key}/${params[key]}`).join('/') : '';
                // !TODO To be fixed in a CR
                // !This should be fixed once the CR for correcting how params are handled for the request
                // !for stations -> '/name/demo' should be something like `?name=demo` or `?name/demo`
                // !unable to use the params serializer, because axios adds a '?' between the url and params
                // GET / params and custom serializer for get parameters
                // request.params = data;
                // requestConfig.paramsSerializer = paramsSerializer;
                request.url = request.url.concat(paramsSerializer(data));
            } else {
                // POST / data

                // If request content-type header is set to msgpack,
                // encode the request body as msgpack
                const requestType = getHeaderValue(request, 'content-type');
                const encodedData = (requestType && requestType === 'application/msgpack') ?
                    msgpack.encode(data) : data;

                if (requestType && requestType === 'application/msgpack') {
                    // tslint:disable-next-line:no-magic-numbers
                    logger.debug(`Encoding request as msgpack buffer length: ${(encodedData.length / (1024 * 1024))}`);
                }
                request.data = encodedData;
            }

        }
        return this.axiosInstance(request).then(response => {
            // If the request is configured to accept msgpack responses,
            // decode the response body from message pack; otherwise return the response
            // body as is (e.g. for JSON or plain text encodings)

            const responseType = getHeaderValue(response, 'content-type');
            const responseData = responseType && responseType === 'application/msgpack' ?
                msgpack.decode(response.data) :
                    response.data;
            return responseData;
        })
        .catch(error => {
            if (error.response) {
                // The request was made and the server responded with a status code
                // outside the range of 2xx
                logger.error(`Error response - status: ${error.response.status}`,
                             `\nheaders: ${JSON.stringify(error.response.headers, undefined, 2)}`);
            } else if (error.request) {
                // The request was made but no response was received
                logger.error(`Error - request: ${JSON.stringify(error.request, undefined, 2)}`);
            } else {
                // Something happened in setting up the request that triggered an Error
                logger.error(`Error - message: ${error.message}`);
            }
            logger.error(`Error in request to: ${JSON.stringify(error.config.url, undefined, 2)}`);
            return {};
        })
        .catch(e => {
            logger.error(`Http Wrapper Failed Request ${request.url}`);
        });
    }

    /**
     * Create & return a new AxiosMockWrapper for this client
     */
    public createHttpMockWrapper(): HttpMockWrapper {
        return this.mockAdaptor = new HttpMockWrapper();
    }
}

/**
 * Gets the header value from request
 * @param httpConfig http config
 * @param headerName header name
 * @returns a value from the header
 */
export function getHeaderValue(httpConfig: any, headerName: string): string {
    let value;
    if (httpConfig && httpConfig.headers) {
        Object.keys(httpConfig.headers).forEach(key => {
            if (key.toLowerCase() === headerName.toLowerCase()) {
                value = httpConfig.headers[key];
            }
        });
    }
    return value;
}
