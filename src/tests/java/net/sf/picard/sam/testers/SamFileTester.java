package net.sf.picard.sam.testers;

import net.sf.picard.cmdline.CommandLineProgram;
import net.sf.picard.io.IoUtil;
import net.sf.samtools.SAMRecordSetBuilder;
import net.sf.samtools.*;
import net.sf.samtools.util.CloseableIterator;
import org.testng.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for doing basic on the fly SAM file testing.
 */
public abstract class SamFileTester {

    public static final String TEST_DATA_BASE_DIR = "testdata/net/sf/picard/sam/";
    private final SAMRecordSetBuilder samRecordSetBuilder;
    protected final Map<String, Boolean> duplicateFlags = new HashMap<String, Boolean>();
    private File outputDir;
    private File output;
    private int readNameCounter = 0;
    private boolean noMateCigars = false;
    private boolean deleteOnExit = true;
    private final ArrayList<String> args = new ArrayList<String>();

    public SamFileTester(final int readLength, final boolean deleteOnExit, final int defaultChromosomeLength) {
        this.deleteOnExit = deleteOnExit;
        this.samRecordSetBuilder = new SAMRecordSetBuilder(true, SAMFileHeader.SortOrder.coordinate, true, defaultChromosomeLength);
        samRecordSetBuilder.setReadLength(readLength);
        setOutputDir();
    }

    public SamFileTester(final int readLength, final boolean deleteOnExit) {
        this.deleteOnExit = deleteOnExit;
        this.samRecordSetBuilder = new SAMRecordSetBuilder();
        samRecordSetBuilder.setReadLength(readLength);
        setOutputDir();
    }

    public int getNumberOfRecords() {
        return this.samRecordSetBuilder.size();
    }

    public CloseableIterator<SAMRecord> getRecordIterator() {
        return this.samRecordSetBuilder.iterator();
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(final File output) {
        this.output = output;
    }

    public void addArg(final String arg) {
        args.add(arg);
    }

    public ArrayList<String> getArgs() {
        return args;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(final File outputDir) {
        this.outputDir = outputDir;
    }

    public void setNoMateCigars(final boolean value) {
        this.noMateCigars = value;
    }

    public boolean getDeleteOnExit() {
        return deleteOnExit;
    }

    protected String samRecordToDuplicatesFlagsKey(final SAMRecord record) {
        String readName = record.getReadName()
                + "-"
                + record.getReadPairedFlag()
                + "-";
        if (record.getReadPairedFlag()) {
            readName += record.getFirstOfPairFlag()
                    + "-"
                    + record.getSecondOfPairFlag();
        } else {
            readName += "false-false";
        }
        return readName;
    }
    // Below are a bunch of utility methods for adding records to the SAMRecordSetBuilder
    public void addUnmappedFragment(final int referenceSequenceIndex,
                                    final int defaultQualityScore) {
        addFragment(referenceSequenceIndex, -1, true, false, null, null, defaultQualityScore, false);
    }

    public void addUnmappedFragment(final int referenceSequenceIndex,
                                    final String qualityString){
        addFragment(referenceSequenceIndex, -1, true, false, null, qualityString, -1, false);
    }

    public void addUnmappedPair(final int referenceSequenceIndex,
                                final int defaultQualityScore) {
        addMatePair(referenceSequenceIndex, -1, -1, true, true, false, false, null, null, false, false, false, false, false, defaultQualityScore);
    }

    public void addMappedFragment(final int referenceSequenceIndex, final int alignmentStart, final boolean isDuplicate,
                                  final int defaultQualityScore) {
        addFragment(referenceSequenceIndex, alignmentStart, false, isDuplicate, null, null, defaultQualityScore, false);
    }

    public void addMappedFragment(final int referenceSequenceIndex, final int alignmentStart, final boolean isDuplicate,
                                  final int defaultQualityScore, final boolean isSecondary) {
        addFragment(referenceSequenceIndex, alignmentStart, false, isDuplicate, null, null, defaultQualityScore, isSecondary);
    }

    public void addMappedFragment(final int referenceSequenceIndex, final int alignmentStart, final boolean isDuplicate, final String cigar,
                                  final int defaultQualityScore) {
        addFragment(referenceSequenceIndex, alignmentStart, false, isDuplicate, cigar, null, defaultQualityScore, false);
    }

    public void addMappedFragment(final int referenceSequenceIndex, final int alignmentStart, final boolean isDuplicate, final String cigar,
                                  final String qualityString,
                                  final int defaultQualityScore) {
        addFragment(referenceSequenceIndex, alignmentStart, false, isDuplicate, cigar, qualityString, defaultQualityScore, false);
    }

    public void addMappedPair(final int referenceSequenceIndex,
                              final int alignmentStart1,
                              final int alignmentStart2,
                              final boolean isDuplicate1,
                              final boolean isDuplicate2,
                              final int defaultQualityScore) {
        addMappedPair(referenceSequenceIndex, alignmentStart1, alignmentStart2, isDuplicate1, isDuplicate2, null, null,
                false, defaultQualityScore);
    }

    public void addMappedPair(final int referenceSequenceIndex,
                              final int alignmentStart1,
                              final int alignmentStart2,
                              final boolean isDuplicate1,
                              final boolean isDuplicate2,
                              final String cigar1,
                              final String cigar2,
                              final boolean firstOnly,
                              final int defaultQualityScore) {
        addMappedPair(referenceSequenceIndex, alignmentStart1, alignmentStart2, isDuplicate1, isDuplicate2, cigar1,
                cigar2, false, true, firstOnly, defaultQualityScore);
    }

    public void addMappedPair(final int referenceSequenceIndex,
                              final int alignmentStart1,
                              final int alignmentStart2,
                              final boolean isDuplicate1,
                              final boolean isDuplicate2,
                              final String cigar1,
                              final String cigar2,
                              final boolean strand1,
                              final boolean strand2,
                              final boolean firstOnly,
                              final int defaultQualityScore) {
        addMatePair(referenceSequenceIndex, alignmentStart1, alignmentStart2, false, false, isDuplicate1, isDuplicate2, cigar1, cigar2,
                strand1, strand2, firstOnly, false, false, defaultQualityScore);
    }

    public void addMatePair(final int referenceSequenceIndex,
                              final int alignmentStart1,
                              final int alignmentStart2,
                              final boolean record1Unmapped,
                              final boolean record2Unmapped,
                              final boolean isDuplicate1,
                              final boolean isDuplicate2,
                              final String cigar1,
                              final String cigar2,
                              final boolean strand1,
                              final boolean strand2,
                              final boolean firstOnly,
                              final boolean record1NonPrimary,
                              final boolean record2NonPrimary,
                              final int defaultQualityScore) {
        addMatePair("READ" + readNameCounter++, referenceSequenceIndex, alignmentStart1, alignmentStart2, record1Unmapped, record2Unmapped,
                isDuplicate1, isDuplicate2, cigar1, cigar2, strand1, strand2, firstOnly, record1NonPrimary, record2NonPrimary,
                defaultQualityScore);
    }

    private void addFragment(final int referenceSequenceIndex, final int alignmentStart, final boolean recordUnmapped, final boolean isDuplicate, final String cigar,
                             final String qualityString, final int defaultQualityScore, final boolean isSecondary) {
        final SAMRecord record = samRecordSetBuilder.addFrag("READ" + readNameCounter++, referenceSequenceIndex, alignmentStart, false,
                recordUnmapped, cigar, qualityString, defaultQualityScore, isSecondary);

        this.duplicateFlags.put(samRecordToDuplicatesFlagsKey(record), isDuplicate);
    }

    public void addMatePair(final String readName,
                            final int referenceSequenceIndex,
                            final int alignmentStart1,
                            final int alignmentStart2,
                            final boolean record1Unmapped,
                            final boolean record2Unmapped,
                            final boolean isDuplicate1,
                            final boolean isDuplicate2,
                            final String cigar1,
                            final String cigar2,
                            final boolean strand1,
                            final boolean strand2,
                            final boolean firstOnly,
                            final boolean record1NonPrimary,
                            final boolean record2NonPrimary,
                            final int defaultQuality) {
        final List<SAMRecord> samRecordList = samRecordSetBuilder.addPair(readName, referenceSequenceIndex, alignmentStart1, alignmentStart2,
                record1Unmapped, record2Unmapped, cigar1, cigar2, strand1, strand2, record1NonPrimary, record2NonPrimary, defaultQuality);

        final SAMRecord record1 = samRecordList.get(0);
        final SAMRecord record2 = samRecordList.get(1);

        if (this.noMateCigars) {
            record1.setAttribute("MC", null);
            record2.setAttribute("MC", null);
        }

        if (firstOnly) {
            samRecordSetBuilder.getRecords().remove(record2);
        }

        this.duplicateFlags.put(samRecordToDuplicatesFlagsKey(record1), isDuplicate1);
        this.duplicateFlags.put(samRecordToDuplicatesFlagsKey(record2), isDuplicate2);
    }

    protected abstract void test();

    protected abstract CommandLineProgram getProgram();

    /**
     * Sets up the basic command line arguments for input and output and runs instanceMain.
     */
    public void runTest() {
        if (getProgram() != null) {
            final File input = createInputFile();

            output = new File(outputDir, "output.sam");
            args.add("INPUT=" + input.getAbsoluteFile());
            args.add("OUTPUT=" + output.getAbsoluteFile());
            Assert.assertEquals(getProgram().instanceMain(args.toArray(new String[args.size()])), 0);
        }
        test();
    }

    private File createInputFile() {
        // Create the input file
        final File input = new File(outputDir, "input.sam");
        final SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(samRecordSetBuilder.getHeader(), true, input);
        for (final SAMRecord record : samRecordSetBuilder.getRecords()) {
            writer.addAlignment(record);
        }
        writer.close();
        return input;
    }

    public SAMFileReader getInput(){
        return samRecordSetBuilder.getSamReader();
    }
}