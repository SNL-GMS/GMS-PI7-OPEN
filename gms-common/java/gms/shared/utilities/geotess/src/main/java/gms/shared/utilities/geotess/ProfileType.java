package gms.shared.utilities.geotess;

/**
 * There are 5 types of profiles:
 * <ol start="0">
 * <li>EMPTY : two radii and no Data
 * <li>THIN: one radius and one Data
 * <li>CONSTANT: two radii and one Data
 * <li>NPOINT: two or more radii and an equal number of Data
 * <li>SURFACE: no radii and one Data
 * </ol>
 * <p>
 * 
 * @author Sandy Ballard Ballard
 * 
 */
public enum ProfileType
{

	// DO NOT CHANGE THE ORDER OF THE ProfileTypes!
	/**
	 * A profile defined by two radii and no Data
	 */
	EMPTY,

	/**
	 * A profile defined by a single radius and one Data object
	 */
	THIN,

	/**
	 * A profile defined by two radii and one Data object
	 */
	CONSTANT,

	/**
	 * A profile defined by two or more radii and an equal number of Data
	 * objects.
	 */
	NPOINT,

	/**
	 * A profile defined by no radii and one Data object
	 */
	SURFACE,

	/**
	 * A profile defined by no radii and no Data
	 */
	SURFACE_EMPTY

};

