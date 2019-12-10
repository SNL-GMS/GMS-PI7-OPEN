import { HttpLink } from 'apollo-link-http';
import * as msgpack from 'msgpack-lite';

export const msgPackfetcher: GlobalFetch['fetch'] = async (uri, options) => {
  const response = await fetch(uri, options);
  // const headers = response.headers.get('Content-Type');
  try {
    // TODO: unable to properly set the content-type from apollo-server
    // if (headers !== null && (headers.includes('application/msgpack'))) {
    const encoded = await response.json();
    const decoded = msgpack.decode(encoded.data.data);
    return new Response(JSON.stringify({ data: decoded }), response);
    // }
  } catch (error) {
    // tslint:disable-next-line:no-console
    console.error(`Response errr: ${error}`);
  }
  const body = await response.text();
  return new Response(body, response);
};

export const bactchMsgPackfetcher: GlobalFetch['fetch'] = async (uri, options) => {
  const response = await fetch(uri, options);
  // const headers = response.headers.get('Content-Type');
  try {
    // TODO: unable to properly set the content-type from apollo-server
    // if (headers !== null && (headers.includes('application/msgpack'))) {
    const encoded: any[] = await response.json();
    const data: any[] = [];
    encoded.forEach(d => {
      const decoded = msgpack.decode(d.data.data);
      data.push({ data: decoded });
    });
    return new Response(JSON.stringify(data), response);
    // }
  } catch (error) {
    // tslint:disable-next-line:no-console
    console.error(`Response errr: ${error}`);
  }
  const body = await response.text();
  return new Response(body, response);
};

export const MsgPackLink = (url: string): HttpLink | undefined => {
  try {
    return new HttpLink({
      uri: url,
      fetch: msgPackfetcher,
      headers: {
        'Accept-Encoding': 'gzip, deflate, br',
        Accept: 'application/json, application/msgpack',
        // apollo client does not allow for overriding the content type
      }
    });
  } catch (error) {
    // tslint:disable-next-line:no-console
    console.log(`Failed to create MsgPack HTTP Link: ${error}`);
    return undefined;
  }
};
