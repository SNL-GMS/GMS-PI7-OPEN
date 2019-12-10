package gms.dataacquisition.seedlink.clientlibrary.data.formats;

public class Integrator {

  public static void integrate(int[] data, int start, int end)
  {
    if (start >= data.length)
      return;

    int prev = data[start];

    for (start++; start < end; start++)
    {
      prev += data[start];
      data[start] = prev;
    }
  }

}
