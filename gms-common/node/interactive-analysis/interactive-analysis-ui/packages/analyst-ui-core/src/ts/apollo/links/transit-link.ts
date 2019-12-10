import { HttpLink } from 'apollo-link-http';

import { transitReader, transitToObj } from '../util/transit-util';

const fetcher: GlobalFetch['fetch'] = async (uri, options) => {
  const response = await fetch(uri, options);
  const headers = response.headers.get('Content-Type');
  const body = await response.text();
  const newBody =
    headers != null && headers.includes('application/transit+json')
      ? JSON.stringify(transitToObj(transitReader.read(body)))
      : body;
  return new Response(newBody, response);
};

export const TransitLink = (url: string): HttpLink | undefined => {
  try {
    return new HttpLink({
      uri: url,
      headers: {
        'Accept-Encoding': 'gzip, deflate, br',
        Accept: 'application/json, application/transit+json'
        // apollo client does not allow for overriding the content type
      },
      fetch: fetcher
    });
  } catch (error) {
    // tslint:disable-next-line:no-console
    console.log(`Failed to create Transit HTTP Link: ${error}`);
    return undefined;
  }
};
