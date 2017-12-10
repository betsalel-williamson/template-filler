package com.templateFiller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Copyright (c) 12/1/17 Betsalel Williamson
 */
public class MainController implements Initializable {

    private static final Logger logger = Logger.getLogger(MainController.class);

    public TableView urlTable;

    public TabPane mainWindow;

    private final HashMap<String, String> mappings = new HashMap<String, String>();

    @FXML
    public TextField yourFirstNameTxt, yourLastNameTxt, yourNickNameTxt, yourPhoneNumberTxt, yourEmailTxt, yourTitleTxt, yourCompanyTxt, theirFirstNameTxt, theirLastNameTxt, theirNickNameTxt, theirPhoneNumberTxt, theirEmailTxt, theirTitleTxt, theirCompanyTxt;

    @FXML
    public TextArea howYouMetTxt;

    @FXML
    public GridPane theirInfo, yourInfo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<String> urls = FXCollections.observableArrayList();

        // todo fix urls
        this.urlTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.urlTable.setItems(urls);

        Preferences prefs = Preferences.userNodeForPackage(MainController.class);

        //
        for (Node node : this.yourInfo.getChildren())
        {
            if (node instanceof TextField)
            {
                MainController.logger.debug("Id: " + node.getId());
                // clear
//                ((TextField)node).setText("");
            }
        }

        for (Node node : this.theirInfo.getChildren())
        {
            if (node instanceof TextField)
            {
                MainController.logger.debug("Id: " + node.getId());
                // clear
//                ((TextField)node).setText("");
            }
        }
    }


    /**
     * There are at least 3 approaches for replacing variables in
     * a docx.
     * <p>
     * 1. as shows in this example (but consider VariableReplaceStAX instead!)
     * 2. using Merge Fields (see org.docx4j.model.fields.merge.MailMerger)
     * 3. binding content controls to an XML Part (via XPath)
     * <p>
     * Approach 3 is the recommended one when using docx4j. See the
     * ContentControl* examples, Getting Started, and the subforum.
     * <p>
     * Approach 1, as shown in this example, works in simple cases
     * only.  It won't work if your KEY is split across separate
     * runs in your docx (which often happens), or if you want
     * to insert images, or multiple rows in a table.
     * <p>
     * You're encouraged to investigate binding content controls
     * to an XML part.  There is org.docx4j.model.datastorage.migration.FromVariableReplacement
     * to automatically convert your templates to this better
     * approach.
     * <p>
     * OK, enough preaching.  If you want to use VariableReplace,
     * your variables should be appear like so: ${key1}, ${key2}
     * <p>
     * And if you are having problems with your runs being split,
     * VariablePrepare can clean them up.
     */
    private void replaceDocxVariables(InputStream inputStream, String outputFileName, HashMap<String, String> mappings) throws Docx4JException, JAXBException {

        long start = System.currentTimeMillis();

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputStream);

        try
        {
            VariablePrepare.prepare(wordMLPackage);

        }

        catch (Exception e)
        {
            MainController.logger.error(e.getMessage(), e);
        }


        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();


        // Approach 1 (from 3.0.0; faster if you haven't yet caused unmarshalling to occur):

        documentPart.variableReplace(mappings);

//		// Approach 2 (original)
//
//			// unmarshallFromTemplate requires string input
//			String xml = XmlUtils.marshaltoString(documentPart.getJaxbElement(), true);
//			// Do it...
//			Object obj = XmlUtils.unmarshallFromTemplate(xml, mappings);
//			// Inject result into docx
//			documentPart.setJaxbElement((TemplateDocument) obj);

        // Save it
        wordMLPackage.save(new File(outputFileName));

        long end = System.currentTimeMillis();
        long total = end - start;

        MainController.logger.debug("Time (ms): " + total);
        MainController.logger.debug("Saved to: " + outputFileName);

    }

    private void replacePlainTextVariables(InputStream inputStream, String outputFileName, HashMap<String, String> mappings) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String content = reader.lines().reduce("", String::concat);
        reader.close();

        StrSubstitutor sub = new StrSubstitutor(mappings);
        String resolvedString = sub.replace(content);

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
        writer.write(resolvedString);
        writer.close();
    }

    @FXML
    public void generateReport(ActionEvent actionEvent) {

        // todo auto update mappings as they are entered and save them to settings
        // todo move the mappings to a file to make them easier to identify
        HashMap<String, String> mappings = new HashMap<String, String>();

        /// your information
        mappings.put("Your Full Name",
                (this.yourFirstNameTxt.getText() + " "
                        + (this.yourNickNameTxt.getText().isEmpty() ? " " : "\""
                        + this.yourNickNameTxt.getText() + "\" ")
                        + this.yourLastNameTxt.getText()).trim());

        mappings.put("Your Phone Number", this.yourPhoneNumberTxt.getText().trim());

        // for cases where you want to put your email in the document more than once
        mappings.put("Your Email Address", this.yourEmailTxt.getText().trim());
        mappings.put("Your Email Address 1", this.yourEmailTxt.getText().trim());
        mappings.put("Your Email Address 2", this.yourEmailTxt.getText().trim());

        mappings.put("Your Title", this.yourTitleTxt.getText().trim());

        mappings.put("Your Company", this.yourCompanyTxt.getText().trim());

        /// their information
        mappings.put("Their Full Name", (this.theirFirstNameTxt.getText() + " "
                + (this.theirNickNameTxt.getText().isEmpty() ? " " : " \""
                + this.theirNickNameTxt.getText() + "\" ")
                + this.theirLastNameTxt.getText()).trim());

        mappings.put("Their Phone Number", this.theirPhoneNumberTxt.getText().trim());

        mappings.put("Their Email Address", this.theirEmailTxt.getText().trim());

        mappings.put("Their Title", this.theirTitleTxt.getText().trim());

        mappings.put("Their Company", this.theirCompanyTxt.getText().trim());

        mappings.put("How You Met", this.howYouMetTxt.getText().trim());

        String dateString = java.time.LocalDate.now().toString();
        mappings.put("Today's Date", dateString);
        mappings.put("Today’s Date", dateString); // for auto replace office indicents

        DirectoryChooser directoryChooser = new DirectoryChooser();

        directoryChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );

        File selectedDirectory = directoryChooser.showDialog(this.mainWindow.getScene().getWindow());

        if (selectedDirectory != null)
        {
            String outputDirectory = selectedDirectory.getAbsolutePath() + File.separatorChar + this.theirCompanyTxt.getText().replaceAll("[^a-zA-Z0-9\\-]", "_");

            // if directory already exists create a new one
            int version = 1;
            String temp = outputDirectory;
            while (!new File(temp).mkdirs())
            {
                temp = outputDirectory + '_' + version++;
            }
            outputDirectory = temp;

            try
            {
                // todo replace with url from table
                URL url = new URL("https://github.com/bhw7/template-filler/blob/develop/sample-docs/sample.docx?raw=true");

                // todo replace with handling result based on doc and using document name from URL
                this.replaceDocxVariables(url.openStream(), outputDirectory + File.separator + "sample.docx", mappings);

                if (Desktop.isDesktopSupported())
                {
                    try
                    {
                        Desktop.getDesktop().open(new File(outputDirectory));
                    }
                    catch (IOException e)
                    {
                        MainController.logger.error(e.getMessage(), e);
                    }
                }
            }

            catch (Docx4JException | JAXBException | IOException e)
            {
                MainController.logger.error(e.getMessage(), e);
            }

        }
    }
}
