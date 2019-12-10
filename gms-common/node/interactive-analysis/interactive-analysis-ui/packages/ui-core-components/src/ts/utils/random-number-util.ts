// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const random = require('math-random');

/**
 * Returns a random rumber that is a 
 * cryptographically secure random number generation.
 * 
 * The number returned will be between 0 - 1.
 *  
 * A Cryptographically Secure Pseudo-Random Number Generator.
 * This is what produces unpredictable data that you need for security purposes.
 * use of Math.random throughout codebase is considered high criticality
 * At present, the only required use is a simple random number.
 * If additional functionality is required in the future,
 * a random number library can be created to support more
 * sophisticated usage.
 */
// tslint:disable-next-line:no-unnecessary-callback-wrapper
export const getSecureRandomNumber = () => random();
