/**
 * Contains a plugin wrapper around the algorithm used to create QcMasks from acquired channel
 * state-of-health information.  Also has an algorithm to merge the QcMasks created by the plugin
 * with existing QcMasks when the masks overlap in time and mask the same data quality issue (see
 * gms.core.waveformqc.channelsohqc.plugin.MergeQcMasks).
 */
package gms.core.waveformqc.channelsohqc.plugin;