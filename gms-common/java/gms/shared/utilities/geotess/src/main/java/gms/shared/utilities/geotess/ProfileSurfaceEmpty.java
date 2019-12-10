package gms.shared.utilities.geotess;

import static gms.shared.utilities.geotess.util.globals.Globals.NL;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public class ProfileSurfaceEmpty extends Profile
{

	/**
	 * Default constructor
	 */
	public ProfileSurfaceEmpty()
	{
	}

	/**
	 * Constructor that loads required information from an ascii file.
	 * 
	 * @param input
	 * @throws GeoTessException
	 * @throws GeoTessException
	 */
	protected ProfileSurfaceEmpty(Scanner input) throws GeoTessException
	{
		// do nothing
	}

//	/**
//	 * Constructor that loads required information from netcdf Iterator objects.
//	 * 
//	 * @param itRadii
//	 * @throws GeoTessException
//	 */
//	protected ProfileEmpty(IndexIterator itRadii) throws GeoTessException
//	{
//		this(itRadii.getFloatNext(), itRadii.getFloatNext());
//	}

	/**
	 * Constructor that loads required information from a binary file.
	 * 
	 * @param input
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected ProfileSurfaceEmpty(DataInputStream input) throws GeoTessException,
			IOException
	{
		// do nothing
	}

	@Override
	public ProfileType getType()
	{
		return ProfileType.SURFACE_EMPTY;
	}

	@Override
	public boolean equals(Object other)
	{
		return other != null && other instanceof ProfileSurfaceEmpty;
	}

	@Override
	public boolean isNaN(int nodeIndex, int attributeIndex)
	{
		return true;
	}

	@Override
	public double getValue(int attributeIndex, int nodeIndex)
	{
		return Double.NaN;
	}

	@Override
	public double getValueTop(int attributeIndex)
	{
		return Double.NaN;
	}

	@Override
	public double getRadius(int i)
	{
		return Double.NaN;
	}

	@Override
	public void setRadius(int node, float radius) { /* do nothing */ }
	
	@Override
	public Data[] getData()
	{
		return new Data[0];
	}

	@Override
	public Data getData(int i)
	{
		return null;
	}

	@Override
	public void setData(Data... data)
	{
		// do nothing
	}

	@Override
	public void setData(int index, Data data)
	{
		// do nothing
	}

	@Override
	public double getRadiusTop()
	{
		return Double.NaN;
	}

	@Override
	public Data getDataTop()
	{
		return null;
	}

	@Override
	public double getRadiusBottom()
	{
		return Double.NaN;
	}

	@Override
	public Data getDataBottom()
	{
		return null;
	}

	@Override
	public int getNRadii()
	{
		return 0;
	}

	@Override
	public int getNData()
	{
		return 0;
	}

	@Override
	public float[] getRadii()
	{
		return new float[0];
	}

	@Override
	protected void write(Writer output) throws IOException
	{
		output.write(String.format("%d%n", getType().ordinal()));
	}

	@Override
	protected void write(DataOutputStream output) throws IOException
	{
		output.writeByte((byte) getType().ordinal());
	}

	@Override
	public int findClosestRadiusIndex(double radius)
	{
		return -1;
	}

	@Override
	public void setPointIndex(int nodeIndex, int pointIndex)
	{
	}

	@Override
	public int getPointIndex(int nodeIndex)
	{
		return -1;
	}

	/**
	 * Returns an independent deep copy of this profile.
	 */
	@Override
	public Profile copy() throws GeoTessException
	{
		return new ProfileSurfaceEmpty();
	}

	@Override
	public void resetPointIndices()
	{
	}

	/**
	 * Outputs this Profile as a formatted string.
	 */
	@Override
	public String toString()
	{
		return "  Type: "  + getType().name() + NL;
	}

}
