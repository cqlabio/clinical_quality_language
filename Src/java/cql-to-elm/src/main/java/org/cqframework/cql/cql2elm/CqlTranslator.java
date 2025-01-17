package org.cqframework.cql.cql2elm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.antlr.v4.runtime.*;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.cql2elm.model.serialization.LibraryWrapper;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.cql_annotations.r1.CqlToElmBase;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.hl7.elm_modelinfo.r1.TypeSpecifier;

import javax.xml.bind.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.FileVisitResult.CONTINUE;

public class CqlTranslator {
    public static enum Format { XML, JSON, COFFEE }
    private static JAXBContext jaxbContext;
    private static ObjectMapper jsonMapper;

    private CqlCompiler compiler;

    public static CqlTranslator fromText(String cqlText, ModelManager modelManager, LibraryManager libraryManager, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(null, CharStreams.fromString(cqlText), modelManager, libraryManager, null, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromText(NamespaceInfo namespaceInfo, String cqlText, ModelManager modelManager, LibraryManager libraryManager, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(namespaceInfo, CharStreams.fromString(cqlText), modelManager, libraryManager, null, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromText(String cqlText, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(null, CharStreams.fromString(cqlText), modelManager, libraryManager, null, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromText(NamespaceInfo namespaceInfo, String cqlText, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(namespaceInfo, CharStreams.fromString(cqlText), modelManager, libraryManager, null, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromText(String cqlText, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(null, CharStreams.fromString(cqlText), modelManager, libraryManager, null, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromText(NamespaceInfo namespaceInfo, String cqlText, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(namespaceInfo, CharStreams.fromString(cqlText), modelManager, libraryManager, null, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromText(String cqlText, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(null, CharStreams.fromString(cqlText), modelManager, libraryManager, ucumService, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromText(NamespaceInfo namespaceInfo, String cqlText, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(namespaceInfo, CharStreams.fromString(cqlText), modelManager, libraryManager, ucumService, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromText(String cqlText, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(null, CharStreams.fromString(cqlText), modelManager, libraryManager, ucumService, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromText(NamespaceInfo namespaceInfo, String cqlText, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(namespaceInfo, CharStreams.fromString(cqlText), modelManager, libraryManager, ucumService, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromText(String cqlText, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(null, CharStreams.fromString(cqlText), modelManager, libraryManager, ucumService, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromText(NamespaceInfo namespaceInfo, String cqlText, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) {
        return new CqlTranslator(namespaceInfo, CharStreams.fromString(cqlText), modelManager, libraryManager, ucumService, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromStream(InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager, CqlTranslatorOptions.Options... options) throws IOException {

        return new CqlTranslator(null, CharStreams.fromStream(cqlStream), modelManager, libraryManager, null, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromStream(NamespaceInfo namespaceInfo, InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, CharStreams.fromStream(cqlStream), modelManager, libraryManager, null, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromStream(InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager,
                                           CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, CharStreams.fromStream(cqlStream), modelManager, libraryManager, null, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromStream(NamespaceInfo namespaceInfo, InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager,
                                           CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, CharStreams.fromStream(cqlStream), modelManager, libraryManager, null, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromStream(InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager,
                                           CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, CharStreams.fromStream(cqlStream), modelManager, libraryManager, null, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromStream(NamespaceInfo namespaceInfo, InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager,
                                           CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, CharStreams.fromStream(cqlStream), modelManager, libraryManager, null, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromStream(InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, CharStreams.fromStream(cqlStream), modelManager, libraryManager, ucumService, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromStream(NamespaceInfo namespaceInfo, InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, CharStreams.fromStream(cqlStream), modelManager, libraryManager, ucumService, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromStream(InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                           CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, CharStreams.fromStream(cqlStream), modelManager, libraryManager, ucumService, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromStream(NamespaceInfo namespaceInfo, InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                           CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, CharStreams.fromStream(cqlStream), modelManager, libraryManager, ucumService, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromStream(InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                           CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, CharStreams.fromStream(cqlStream), modelManager, libraryManager, ucumService, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromStream(NamespaceInfo namespaceInfo, InputStream cqlStream, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                           CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, CharStreams.fromStream(cqlStream), modelManager, libraryManager, ucumService, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromFile(String cqlFileName, ModelManager modelManager, LibraryManager libraryManager, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, null, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, String cqlFileName, ModelManager modelManager, LibraryManager libraryManager, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, null, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(String cqlFileName, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, null, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, String cqlFileName, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, null, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(String cqlFileName, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, null, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, String cqlFileName, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, null, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromFile(File cqlFile, ModelManager modelManager, LibraryManager libraryManager, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, null, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, File cqlFile, ModelManager modelManager, LibraryManager libraryManager, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, null, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(File cqlFile, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, null, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, File cqlFile, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, null, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(File cqlFile, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, null, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, File cqlFile, ModelManager modelManager, LibraryManager libraryManager,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, null, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromFile(String cqlFileName, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, ucumService, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, String cqlFileName, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, ucumService, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(String cqlFileName, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, ucumService, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, String cqlFileName, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, ucumService, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(String cqlFileName, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, ucumService, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, String cqlFileName, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFileName), CharStreams.fromStream(new FileInputStream(cqlFileName)), modelManager, libraryManager, ucumService, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromFile(File cqlFile, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, ucumService, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, File cqlFile, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, ucumService, CqlCompilerException.ErrorSeverity.Info, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(File cqlFile, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, ucumService, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, File cqlFile, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, ucumService, errorLevel, LibraryBuilder.SignatureLevel.None, options);
    }

    public static CqlTranslator fromFile(File cqlFile, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, ucumService, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, File cqlFile, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                                         CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, ucumService, errorLevel, signatureLevel, options);
    }

    public static CqlTranslator fromText(String cqlText, ModelManager modelManager,
                                         LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) {
        return new CqlTranslator(null, CharStreams.fromString(cqlText), modelManager, libraryManager, ucumService, options);
    }

    public static CqlTranslator fromText(NamespaceInfo namespaceInfo, String cqlText, ModelManager modelManager,
                                          LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) {
        return new CqlTranslator(namespaceInfo, CharStreams.fromString(cqlText), modelManager, libraryManager, ucumService, options);
    }

    public static CqlTranslator fromText(NamespaceInfo namespaceInfo, VersionedIdentifier sourceInfo, String cqlText, ModelManager modelManager,
                                         LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) {
        return new CqlTranslator(namespaceInfo, sourceInfo, CharStreams.fromString(cqlText), modelManager, libraryManager, ucumService, options);
    }

    public static CqlTranslator fromStream(InputStream cqlStream, ModelManager modelManager,
                                           LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) throws IOException {
        return new CqlTranslator(null, CharStreams.fromStream(cqlStream), modelManager, libraryManager, ucumService, options);
    }

    public static CqlTranslator fromStream(NamespaceInfo namespaceInfo, InputStream cqlStream, ModelManager modelManager,
                                           LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) throws IOException {
        return new CqlTranslator(namespaceInfo, CharStreams.fromStream(cqlStream), modelManager, libraryManager, ucumService, options);
    }

    public static CqlTranslator fromStream(NamespaceInfo namespaceInfo, VersionedIdentifier sourceInfo, InputStream cqlStream, ModelManager modelManager,
                                           LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) throws IOException {
        return new CqlTranslator(namespaceInfo, sourceInfo, CharStreams.fromStream(cqlStream), modelManager, libraryManager, ucumService, options);
    }

    public static CqlTranslator fromFile(File cqlFile, ModelManager modelManager,
                                         LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) throws IOException {
        return new CqlTranslator(null, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, ucumService, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, File cqlFile, ModelManager modelManager,
                                         LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) throws IOException {
        return new CqlTranslator(namespaceInfo, getSourceInfo(cqlFile), CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, ucumService, options);
    }

    public static CqlTranslator fromFile(NamespaceInfo namespaceInfo, VersionedIdentifier sourceInfo, File cqlFile, ModelManager modelManager,
                                         LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) throws IOException {
        return new CqlTranslator(namespaceInfo, sourceInfo, CharStreams.fromStream(new FileInputStream(cqlFile)), modelManager, libraryManager, ucumService, options);
    }

    private CqlTranslator(NamespaceInfo namespaceInfo, VersionedIdentifier sourceInfo, CharStream is, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                          CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) {
        this(namespaceInfo, sourceInfo, is, modelManager, libraryManager, ucumService, new CqlTranslatorOptions(errorLevel, signatureLevel, options));
    }

    private CqlTranslator(NamespaceInfo namespaceInfo, CharStream is, ModelManager modelManager, LibraryManager libraryManager, UcumService ucumService,
                          CqlCompilerException.ErrorSeverity errorLevel, LibraryBuilder.SignatureLevel signatureLevel, CqlTranslatorOptions.Options... options) {
        this(namespaceInfo, is, modelManager, libraryManager, ucumService, new CqlTranslatorOptions(errorLevel, signatureLevel, options));
    }

    private CqlTranslator(NamespaceInfo namespaceInfo, CharStream is, ModelManager modelManager,
                          LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) {
        this(namespaceInfo, null, is, modelManager, libraryManager, ucumService, options);
    }

    private CqlTranslator(NamespaceInfo namespaceInfo, VersionedIdentifier sourceInfo, CharStream is, ModelManager modelManager,
                          LibraryManager libraryManager, UcumService ucumService, CqlTranslatorOptions options) {
        compiler = new CqlCompiler(namespaceInfo, sourceInfo, modelManager, libraryManager, ucumService);
        compiler.run(is, options);
    }

    private static VersionedIdentifier getSourceInfo(String cqlFileName) {
        return getSourceInfo(new File(cqlFileName));
    }

    private static VersionedIdentifier getSourceInfo(File cqlFile) {
        String name = cqlFile.getName();
        int extensionIndex = name.lastIndexOf('.');
        if (extensionIndex > 0) {
            name = name.substring(0, extensionIndex);
        }
        String system = null;
        try {
            system = cqlFile.getCanonicalPath();
        } catch (IOException e) {
            system = cqlFile.getAbsolutePath();
        }

        return new VersionedIdentifier().withId(name).withSystem(system);
    }

    private String toXml(Library library) {
        try {
            return convertToXml(library);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not convert library to XML.", e);
        }
    }



    private String toJson(Library library) {
        try {
            return convertToJson(library);
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert library to JSON using JAXB serializer.", e);
        }
    }

    public String toXml() {
        return toXml(compiler.getLibrary());
    }

    public String toJson() {
        return toJson(compiler.getLibrary());
    }

    public Library toELM() {
        return compiler.getLibrary();
    }

    public CompiledLibrary getTranslatedLibrary() {
        return compiler.getCompiledLibrary();
    }

    public Object toObject() {
        return compiler.toObject();
    }

    public List<Retrieve> toRetrieves() {
        return compiler.toRetrieves();
    }

    public Map<String, Library> getLibraries() {
        return compiler.getLibraries();
    }

    public Map<String, CompiledLibrary> getTranslatedLibraries() {
        return compiler.getCompiledLibraries();
    }

    public Map<String, String> getLibrariesAsXML() {
        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, CompiledLibrary> entry : getTranslatedLibraries().entrySet()) {
            result.put(entry.getKey(), toXml(entry.getValue().getLibrary()));
        }
        return result;
    }

    public Map<String, String> getLibrariesAsJSON() {
        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, CompiledLibrary> entry : getTranslatedLibraries().entrySet()) {
            result.put(entry.getKey(), toJson(entry.getValue().getLibrary()));
        }
        return result;
    }

    public List<CqlCompilerException> getExceptions() { return compiler.getExceptions(); }

    public List<CqlCompilerException> getErrors() { return compiler.getErrors(); }

    public List<CqlCompilerException> getWarnings() { return compiler.getWarnings(); }

    public List<CqlCompilerException> getMessages() { return compiler.getMessages(); }

    public static ObjectMapper getJsonMapper() {
        if (jsonMapper == null) {
            jsonMapper = new JsonMapper().builder()
                    .defaultMergeable(true)
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    .enable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL)
                    .defaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
                    .addModule(new JaxbAnnotationModule())
                    .addMixIn(TypeInfo.class, TypeInfoMixIn.class)
                    .addMixIn(TypeSpecifier.class, TypeSpecifierMixIn.class)
                    .addMixIn(CqlToElmBase.class, CqlToElmBaseMixIn.class)
                    .build();
        }

        return jsonMapper;
    }

    public static String convertToXml(Library library) throws IOException {
        StringWriter writer = new StringWriter();
        ModelInfoXmlReader.writeValue(library, writer);
        return writer.getBuffer().toString();
    }

    public static String convertToJson(Library library) throws JsonProcessingException {
        LibraryWrapper wrapper = new LibraryWrapper();
        wrapper.setLibrary(library);
        return getJsonMapper().writeValueAsString(wrapper);
    }

    public static void loadModelInfo(File modelInfoXML)  {
        try {
            final ModelInfo modelInfo = ModelInfoXmlReader.readValue(modelInfoXML, ModelInfo.class);
            final VersionedIdentifier modelId = new VersionedIdentifier().withId(modelInfo.getName()).withVersion(modelInfo.getVersion());
            final ModelInfoProvider modelProvider = (VersionedIdentifier modelIdentifier) -> modelInfo;
            final ModelInfoLoader modelInfoLoader = new ModelInfoLoader();
            modelInfoLoader.registerModelInfoProvider(modelProvider);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private static void outputExceptions(Iterable<CqlCompilerException> exceptions) {
        for (CqlCompilerException error : exceptions) {
            TrackBack tb = error.getLocator();
            String lines = tb == null ? "[n/a]" : String.format("[%d:%d, %d:%d]",
                    tb.getStartLine(), tb.getStartChar(), tb.getEndLine(), tb.getEndChar());
            System.err.printf("%s:%s %s%n", error.getSeverity(), lines, error.getMessage());
        }
    }

    private static void writeELM(Path inPath, Path outPath, CqlTranslator.Format format, CqlTranslatorOptions options) throws IOException {

        System.err.println("================================================================================");
        System.err.printf("TRANSLATE %s%n", inPath);

        ModelManager modelManager;
        if(options.getOptions().contains(CqlTranslatorOptions.Options.DisableDefaultModelInfoLoad)) {
            modelManager = new ModelManager(false);
        } else {
            modelManager = new ModelManager();
        }

        LibraryManager libraryManager = new LibraryManager(modelManager);
        UcumService ucumService = null;
        if (options.getValidateUnits()) {
            try {
                ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
            } catch (UcumException e) {
                System.err.println("Could not create UCUM validation service:");
                e.printStackTrace();
            }
        }
        modelManager.getModelInfoLoader().registerModelInfoProvider(new DefaultModelInfoProvider(inPath.getParent()), true);
        libraryManager.getLibrarySourceLoader().registerProvider(new DefaultLibrarySourceProvider(inPath.getParent()));
        libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
        CqlTranslator translator = fromFile(inPath.toFile(), modelManager, libraryManager, ucumService, options);
        libraryManager.getLibrarySourceLoader().clearProviders();

        if (translator.getErrors().size() > 0) {
            System.err.println("Translation failed due to errors:");
            outputExceptions(translator.getExceptions());
        } else if (!options.getVerifyOnly()) {
            if (translator.getExceptions().size() == 0) {
                System.err.println("Translation completed successfully.");
            }
            else {
                System.err.println("Translation completed with messages:");
                outputExceptions(translator.getExceptions());
            }
            try (PrintWriter pw = new PrintWriter(outPath.toFile(), "UTF-8")) {
                switch (format) {
                    case COFFEE:
                        pw.print("module.exports = ");
                        pw.println(translator.toJson());
                        break;
                    case JSON:
                        pw.println(translator.toJson());
                        break;
                    case XML:
                    default:
                        pw.println(translator.toXml());
                }
                pw.println();
            }
            System.err.println(String.format("ELM output written to: %s", outPath.toString()));
        }

        System.err.println();
    }

    @SuppressWarnings({ "unchecked", "rawtypes"})
    public static void main(String[] args) throws IOException, InterruptedException {
        OptionParser parser = new OptionParser();
        OptionSpec<File> input = parser.accepts("input").withRequiredArg().ofType(File.class).required().describedAs("The name of the input file or directory. If a directory is given, all files ending in .cql will be processed");
        OptionSpec<File> model = parser.accepts("model").withRequiredArg().ofType(File.class).describedAs("The name of an input file containing the model info to use for translation. Model info can also be provided through an implementation of ModelInfoProvider");
        OptionSpec<File> output = parser.accepts("output").withRequiredArg().ofType(File.class).describedAs("The name of the output file or directory. If no output is given, an output file name is constructed based on the input name and target format");
        OptionSpec<CqlTranslator.Format> format = parser.accepts("format").withRequiredArg().ofType(CqlTranslator.Format.class).defaultsTo(CqlTranslator.Format.XML).describedAs("The target format for the output");
        OptionSpec disableDefaultModelInfoLoad = parser.accepts("disable-default-modelinfo-load");
        OptionSpec verify = parser.accepts("verify");
        OptionSpec optimization = parser.accepts("date-range-optimization");
        OptionSpec annotations = parser.accepts("annotations");
        OptionSpec locators = parser.accepts("locators");
        OptionSpec resultTypes = parser.accepts("result-types");
        OptionSpec detailedErrors = parser.accepts("detailed-errors");
        OptionSpec errorLevel = parser.accepts("error-level").withRequiredArg().ofType(CqlCompilerException.ErrorSeverity.class).defaultsTo(CqlCompilerException.ErrorSeverity.Info).describedAs("Indicates the minimum severity message that will be reported. If no error-level is specified, all messages will be output");
        OptionSpec disableListTraversal = parser.accepts("disable-list-traversal");
        OptionSpec disableListDemotion = parser.accepts("disable-list-demotion");
        OptionSpec disableListPromotion = parser.accepts("disable-list-promotion");
        OptionSpec enableIntervalDemotion = parser.accepts("enable-interval-demotion");
        OptionSpec enableIntervalPromotion = parser.accepts("enable-interval-promotion");
        OptionSpec disableMethodInvocation = parser.accepts("disable-method-invocation");
        OptionSpec requireFromKeyword = parser.accepts("require-from-keyword");
        OptionSpec strict = parser.accepts("strict");
        OptionSpec debug = parser.accepts("debug");
        OptionSpec validateUnits = parser.accepts("validate-units");
        OptionSpec<LibraryBuilder.SignatureLevel> signatures = parser.accepts("signatures").withRequiredArg().ofType(LibraryBuilder.SignatureLevel.class).defaultsTo(LibraryBuilder.SignatureLevel.None).describedAs("Indicates whether signatures should be included for invocations in the output ELM. Differing will include invocation signatures that differ from the declared signature. Overloads will include declaration signatures when the operator or function has more than one overload with the same number of arguments as the invocation");
        OptionSpec<String> compatibilityLevel = parser.accepts("compatibility-level").withRequiredArg().ofType(String.class).describedAs("Compatibility level for the translator, valid values are 1.3, 1.4, and 1.5");

        OptionSet options = parser.parse(args);

        final Path source = input.value(options).toPath();
        final Path destination =
                output.value(options) != null
                        ? output.value(options).toPath()
                        : source.toFile().isDirectory() ? source : source.getParent();
        final CqlTranslator.Format outputFormat = format.value(options);
        final LibraryBuilder.SignatureLevel signatureLevel = signatures.value(options);

        Map<Path, Path> inOutMap = new HashMap<>();
        if (source.toFile().isDirectory()) {
            if (destination.toFile().exists() && ! destination.toFile().isDirectory()) {
                throw new IllegalArgumentException("Output must be a valid folder if input is a folder!");
            }

            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toFile().getName().endsWith(".cql") || file.toFile().getName().endsWith(".CQL")) {
                        Path destinationFolder = destination.resolve(source.relativize(file.getParent()));
                        if (! destinationFolder.toFile().exists() && ! destinationFolder.toFile().mkdirs()) {
                            System.err.printf("Problem creating %s%n", destinationFolder);
                        }
                        inOutMap.put(file, destinationFolder);
                    }
                    return CONTINUE;
                }
            });
        } else {
            inOutMap.put(source, destination);
        }

        for (Map.Entry<Path, Path> inOut : inOutMap.entrySet()) {
            Path in = inOut.getKey();
            Path out = inOut.getValue();
            if (out.toFile().isDirectory()) {
                // Use input filename with ".xml", ".json", or ".coffee" extension
                String name = in.toFile().getName();
                if (name.lastIndexOf('.') != -1) {
                    name = name.substring(0, name.lastIndexOf('.'));
                }
                switch (outputFormat) {
                    case JSON:
                        name += ".json";
                        break;
                    case COFFEE:
                        name += ".coffee";
                        break;
                    case XML:
                    default:
                        name += ".xml";
                        break;

                }
                out = out.resolve(name);
            }

            if (out.equals(in)) {
                throw new IllegalArgumentException("input and output file must be different!");
            }

            if (options.has(model)) {
                final File modelFile = options.valueOf(model);
                if (! modelFile.exists() || modelFile.isDirectory()) {
                    throw new IllegalArgumentException("model must be a valid file!");
                }
                loadModelInfo(modelFile);
            }

            writeELM(in, out, outputFormat, new CqlTranslatorOptions(outputFormat, options.has(optimization),
                    options.has(debug) || options.has(annotations),
                    options.has(debug) || options.has(locators),
                    options.has(debug) || options.has(resultTypes),
                    options.has(verify),
                    options.has(detailedErrors), // Didn't include in debug, maybe should...
                    options.has(errorLevel)
                            ? (CqlCompilerException.ErrorSeverity)options.valueOf(errorLevel)
                            : CqlCompilerException.ErrorSeverity.Info,
                    options.has(strict) || options.has(disableListTraversal),
                    options.has(strict) || options.has(disableListDemotion),
                    options.has(strict) || options.has(disableListPromotion),
                    options.has(enableIntervalDemotion),
                    options.has(enableIntervalPromotion),
                    options.has(strict) || options.has(disableMethodInvocation),
                    options.has(requireFromKeyword),
                    options.has(validateUnits), options.has(disableDefaultModelInfoLoad),
                    signatureLevel,
                    options.has(compatibilityLevel) ? options.valueOf(compatibilityLevel) : null));
        }
    }
}
