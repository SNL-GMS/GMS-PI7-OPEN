/**
 * Reusable formatter for time uncertainty 
 * 
 * @param unc the uncertainty value in seconds
 * 
 * @returns a string with two trailing digits
 */
export function formatUncertainty(unc: number): string {
  if (!unc) {
    return '';
  }
  return `${unc.toFixed(2)} s`;
}
