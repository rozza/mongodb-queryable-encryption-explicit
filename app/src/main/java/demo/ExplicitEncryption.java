/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package demo;

public class ExplicitEncryption {

    /**
     * Defaults to running the single field example use:
     *  `-Dexample=multi` to run the multiple encrypted field example.
     *
     * @param args will use the first arg as the mongo uri
     */
    public static void main(String[] args) {
        String exampleToRun = System.getProperty("example", "single");

        if (exampleToRun.equalsIgnoreCase("single")) {
            ExplicitEncryptionSingleField.main(args);
        } else if (exampleToRun.equalsIgnoreCase("multi")) {
            ExplicitEncryptionMultiField.main(args);
        } else {
            System.out.println("Unrecognized example: " + exampleToRun);
        }
    }
}
