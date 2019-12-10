package gms.dataacquisition.css.converters.flatfilereaders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.dataacquisition.cssreader.data.WfdiscRecord;
import gms.dataacquisition.cssreader.data.WfdiscRecord32;
import gms.dataacquisition.cssreader.flatfilereaders.FlatFileWfdiscReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests WfdiscRecord, which is a class that both represents and parses CSS 3.0 WfdiscRecord files.
 */
public class WfdiscRecordTest {

  private WfdiscRecord32 wfdisc;
  private double comparisonDelta = 0.000001;

  @Before
  public void setup() {
    wfdisc = new WfdiscRecord32();
  }

  // tests on 'sta'
  @Test(expected = Exception.class)
  public void testSetNullSta() {
    wfdisc.setSta(null);
    wfdisc.validate();
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'chan'
  @Test(expected = Exception.class)
  public void testSetNullChan() {
    wfdisc.setChan(null);
    wfdisc.validate();
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'time'
  @Test(expected = Exception.class)
  public void testSetNullTime() {
    wfdisc.setTime(null);
    wfdisc.validate();
  }

  @Test(expected = Exception.class)
  public void testSetTimeWithBadString() {
    wfdisc.setTime("not a number");
  }

  @Test
  public void testSetTime() {
        wfdisc.setTime("1274317219.75");  // epoch seconds with millis, just as in flatfilereaders files
    assertNotNull(wfdisc.getTime());
    Instant expected = Instant.ofEpochMilli((long) (1274317219.75 * 1000L));
    assertEquals(wfdisc.getTime(), expected);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'wfid'
  @Test(expected = Exception.class)
  public void testSetWfIdNull() {
    wfdisc.setWfid(null);
  }

  @Test(expected = Exception.class)
  public void testSetWfIdBad() {
    wfdisc.setWfid("not a number");
  }

  @Test
  public void testSetWfId() {
    wfdisc.setWfid("12345");
    assertEquals(wfdisc.getWfid(), 12345);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'chanid'
  @Test(expected = Exception.class)
  public void testSetChanIdNull() {
    wfdisc.setChanid(null);
  }

  @Test(expected = Exception.class)
  public void testSetChanIdBad() {
    wfdisc.setChanid("not a number");
  }

  @Test
  public void testSetChanId() {
    wfdisc.setChanid("12345");
    assertEquals(wfdisc.getChanid(), 12345);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'jdate'
  @Test(expected = Exception.class)
  public void testSetJdateNull() {
    wfdisc.setJdate(null);
  }

  @Test(expected = Exception.class)
  public void testSetJdateBad() {
    wfdisc.setJdate("not a number");
  }

  @Test(expected = Exception.class)
  public void testSetJdateTooLow() {
    wfdisc.setJdate("1500001"); // ah, the first day of 1500 AD.  What a glorious time to be alive.
    wfdisc.validate();
  }

  @Test(expected = Exception.class)
  public void testSetJdateTooHigh() {
    wfdisc.setJdate("2900001"); // If this code is still running in 2900 AD, that's impressive.
    wfdisc.validate();
  }

  @Test
  public void testSetJdate() {
    wfdisc.setJdate("2012123");
    assertEquals(wfdisc.getJdate(), 2012123);
  }
  ////////////////////////////////////////////////////////////////////////
  // tests on 'endtime'

  @Test(expected = Exception.class)
  public void testSetNullEndtime() {
    wfdisc.setEndtime(null);
  }

  @Test(expected = Exception.class)
  public void testSetEndtimeWithBadString() {
    wfdisc.setEndtime("not a number");
  }

  @Test
  public void testSetEndtime() {
    wfdisc.setEndtime("1274317219.75");  // epoch seconds with millis, just as in wfdiscreaders files
    assertNotNull(wfdisc.getEndtime());
    Instant expected = Instant.ofEpochMilli((long) (1274317219.75 * 1000L));
    assertEquals(wfdisc.getEndtime(), expected);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'nsamp'
  @Test(expected = Exception.class)
  public void testSetNsampNull() {
    wfdisc.setNsamp(null);
  }

  @Test(expected = Exception.class)
  public void testSetNsampBad() {
    wfdisc.setNsamp("not a number");
  }

  @Test(expected = Exception.class)
  public void testSetNsampNegative() {
    wfdisc.setNsamp("-123");
    wfdisc.validate();
  }

  @Test
  public void testSetNsamp() {
    wfdisc.setNsamp("5000");
    assertEquals(wfdisc.getNsamp(), 5000);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'samprate'
  @Test(expected = Exception.class)
  public void testSetSamprateNull() {
    wfdisc.setSamprate(null);
  }

  @Test(expected = Exception.class)
  public void testSetSamprateBad() {
    wfdisc.setSamprate("not a number");
  }

  @Test(expected = Exception.class)
  public void testSetSamprateNegative() {
    wfdisc.setSamprate("-123");
    wfdisc.validate();
  }

  @Test
  public void testSetSamprate() {
    wfdisc.setSamprate("40.0");
    assertEquals(wfdisc.getSamprate(), 40.0, 0.0);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'calib'
  @Test(expected = Exception.class)
  public void testSetCalibNull() {
    wfdisc.setCalib(null);
  }

  @Test(expected = Exception.class)
  public void testSetCalibBad() {
    wfdisc.setCalib("not a number");
  }

  @Test(expected = Exception.class)
  public void testSetCalibNegative() {
    wfdisc.setCalib("-123");
    wfdisc.validate();
  }

  @Test
  public void testSetCalib() {
    wfdisc.setCalib("1.0");
    assertEquals(wfdisc.getCalib(), 1.0, 0.0);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'calper'
  @Test(expected = Exception.class)
  public void testSetCalperNull() {
    wfdisc.setCalper(null);
  }

  @Test(expected = Exception.class)
  public void testSetCalperNegative() {
    wfdisc.setCalper("-123");
    wfdisc.validate();
  }

  @Test(expected = Exception.class)
  public void testSetCalperBad() {
    wfdisc.setCalper("not a number");
  }

  @Test
  public void testSetCalper() {
    wfdisc.setCalper("1.0");
    assertEquals(wfdisc.getCalper(), 1.0, 0.0);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'instype'
  @Test(expected = Exception.class)
  public void testSetNullInstype() {
    wfdisc.setInstype(null);
    wfdisc.validate();
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'segtype'
  @Test(expected = Exception.class)
  public void testSetSegtypeNull() {
    wfdisc.setSegtype(null);
    wfdisc.validate();
  }

  @Test(expected = Exception.class)
  public void testSetSegtypeBad() {
    wfdisc.setSegtype("not a valid segtype");
    wfdisc.validate();
  }

  @Test
  public void testSetSegtype() {
    String seg = "o";
    wfdisc.setSegtype(seg);
    assertEquals(wfdisc.getSegtype(), seg);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'datatype'
  @Test(expected = Exception.class)
  public void testSetDatatypeNull() {
    wfdisc.setDatatype(null);
    wfdisc.validate();
  }

  @Test
  public void testSetDatatype() {
    String dt = "s4";
    wfdisc.setDatatype(dt);  // format code from CSS 3.0
    assertEquals(wfdisc.getDatatype(), dt);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'clip'
  @Test
  public void testSetClip() {
    wfdisc.setClip("-");
    assertFalse(wfdisc.getClip());

    wfdisc.setClip("some random string that isn't just 'c'");
    assertFalse(wfdisc.getClip());

    wfdisc.setClip("c");
    assertTrue(wfdisc.getClip());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'dir'
  @Test(expected = Exception.class)
  public void testSetNullDir() {
    wfdisc.setDir(null);
    wfdisc.validate();
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'dfile'
  @Test(expected = Exception.class)
  public void testSetNullDfile() {
    wfdisc.setDfile(null);
    wfdisc.validate();
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'foff'
  @Test(expected = Exception.class)
  public void testSetFoffNull() {
    wfdisc.setFoff(null);
  }

  @Test(expected = Exception.class)
  public void testSetFoffBad() {
    wfdisc.setFoff("not a number");
  }

  @Test(expected = Exception.class)
  public void testSetFoffNegative() {
    wfdisc.setFoff("-123");
    wfdisc.validate();
  }

  @Test
  public void testSetFoff() {
    wfdisc.setFoff("12345");
    assertEquals(wfdisc.getFoff(), 12345);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'commid'
  @Test(expected = Exception.class)
  public void testSetCommidNull() {
    wfdisc.setCommid(null);
  }

  @Test(expected = Exception.class)
  public void testSetCommidBad() {
    wfdisc.setCommid("not a number");
  }

  @Test
  public void testSetCommid() {
    wfdisc.setCommid("12345");
    assertEquals(wfdisc.getCommid(), 12345);
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'lddate'
  @Test(expected = Exception.class)
  public void testSetNullLddate() {
    wfdisc.setLddate(null);
    wfdisc.validate();
  }

  // TODO: if ldddate gets read into a Date or something in the future,
  // add a test verifying this works.
  ////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////
    // tests reading flatfilereaders against known values
  @Test
  public void testWfdiscReadAll() throws Exception {
    FlatFileWfdiscReader reader = new FlatFileWfdiscReader();
    List<WfdiscRecord> wfdiscs = reader.read("src/test/resources/css/WFS4/wfdisc_gms_s4.txt");
    assertEquals(wfdiscs.size(), 76);

    WfdiscRecord wfdisc = wfdiscs.get(1);
    assertEquals(wfdisc.getSta(), "DAVOX");
    assertEquals(wfdisc.getChan(), "HHE");
    assertEquals(wfdisc.getTime(), Instant.ofEpochMilli(1274317199108l));
    assertEquals(wfdisc.getWfid(), 64583325);
    assertEquals(wfdisc.getChanid(), 4248);
    assertEquals(wfdisc.getJdate(), 2010140);
    assertEquals(wfdisc.getEndtime(), Instant.ofEpochMilli(1274317201991l));
    assertEquals(wfdisc.getNsamp(), 347);
    assertEquals(wfdisc.getSamprate(), 119.98617, this.comparisonDelta);
    assertEquals(wfdisc.getCalib(), 0.253, this.comparisonDelta);
    assertEquals(wfdisc.getCalper(), 1, this.comparisonDelta);
    assertEquals(wfdisc.getInstype(), "STS-2");
    assertEquals(wfdisc.getSegtype().toString(), "o");
    assertEquals(wfdisc.getDatatype(), "s4");
    assertEquals(wfdisc.getClip(), false);
    assertEquals(wfdisc.getDir(), "src/test/resources/css/WFS4/");
    assertEquals(wfdisc.getDfile(), "DAVOX0.w");
    assertEquals(wfdisc.getFoff(), 63840);
    assertEquals(wfdisc.getCommid(), -1);
    assertEquals(wfdisc.getLddate(), "08-APR-15");
  }

  @Test
  public void testWfdiscReadFilter() throws Exception {
    ArrayList<String> stations = new ArrayList<>(1);
    stations.add("MLR");

    ArrayList<String> channels = new ArrayList<>(1);
    channels.add("BHZ");

    FlatFileWfdiscReader reader = new FlatFileWfdiscReader(stations, channels, null, null);
    List<WfdiscRecord> wfdiscs = reader.read("src/test/resources/css/WFS4/wfdisc_gms_s4.txt");
    assertEquals(wfdiscs.size(), 3);

    WfdiscRecord wfdisc = wfdiscs.get(1);
    assertEquals(wfdisc.getSta(), "MLR");
    assertEquals(wfdisc.getChan(), "BHZ");
    assertEquals(wfdisc.getTime(), Instant.ofEpochMilli(1274317190019l));
    assertEquals(wfdisc.getWfid(), 64587196);
    assertEquals(wfdisc.getChanid(), 4162);
    assertEquals(wfdisc.getJdate(), 2010140);
    assertEquals(wfdisc.getEndtime(), Instant.ofEpochMilli(1274317209994l));
    assertEquals(wfdisc.getNsamp(), 800);
    assertEquals(wfdisc.getSamprate(), 40, this.comparisonDelta);
    assertEquals(wfdisc.getCalib(), 0.0633, this.comparisonDelta);
    assertEquals(wfdisc.getCalper(), 1, this.comparisonDelta);
    assertEquals(wfdisc.getInstype(), "STS-2");
    assertEquals(wfdisc.getSegtype().toString(), "o");
    assertEquals(wfdisc.getDatatype(), "s4");
    assertEquals(wfdisc.getClip(), false);
    assertEquals(wfdisc.getDir(), "src/test/resources/css/WFS4/");
    assertEquals(wfdisc.getDfile(), "MLR0.w");
    assertEquals(wfdisc.getFoff(), 2724800);
    assertEquals(wfdisc.getCommid(), -1);
    assertEquals(wfdisc.getLddate(), "08-APR-15");
  }
}
