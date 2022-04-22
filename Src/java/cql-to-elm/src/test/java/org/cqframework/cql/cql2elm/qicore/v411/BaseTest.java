package org.cqframework.cql.cql2elm.qicore.v411;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.TestUtils;
import org.hl7.elm.r1.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class BaseTest {
    @BeforeClass
    public void Setup() {
        // Reset test utils to clear any models loaded by other tests
        TestUtils.reset();
    }

    @Test
    public void testQICore() throws IOException {
        CqlTranslator translator = TestUtils.runSemanticTest("qicore/v411/TestQICore.cql", 0);

        Library library = translator.toELM();
        Map<String, ExpressionDef> defs = new HashMap<>();

        if (library.getStatements() != null) {
            for (ExpressionDef def : library.getStatements().getDef()) {
                defs.put(def.getName(), def);
            }
        }

        ExpressionDef def = defs.get("TestAdverseEvent");
        assertThat(def.getExpression(), instanceOf(Retrieve.class));
        Retrieve retrieve = (Retrieve)def.getExpression();
        assertThat(retrieve.getTemplateId(), is("http://hl7.org/fhir/us/qicore/StructureDefinition/qicore-adverseevent"));
    }

    @Test
    public void testEXM124() throws IOException {
        CqlTranslator translator = TestUtils.runSemanticTest("qicore/v411/EXM124_QICore4-8.2.000.cql", 0);

        Library library = translator.toELM();
        Map<String, ExpressionDef> defs = new HashMap<>();

        if (library.getStatements() != null) {
            for (ExpressionDef def : library.getStatements().getDef()) {
                defs.put(def.getName(), def);
            }
        }

        ExpressionDef def = defs.get("Initial Population");
        assertThat(def, notNullValue());
    }

    @Test
    public void testEXM165() throws IOException {
        CqlTranslator translator = TestUtils.runSemanticTest("qicore/v411/EXM165_QICore4-8.5.000.cql", 0);

        Library library = translator.toELM();
        Map<String, ExpressionDef> defs = new HashMap<>();

        if (library.getStatements() != null) {
            for (ExpressionDef def : library.getStatements().getDef()) {
                defs.put(def.getName(), def);
            }
        }

        ExpressionDef def = defs.get("Initial Population");
        assertThat(def, notNullValue());
    }

    @Test
    public void testAdultOutpatientEncounters() throws IOException {
        CqlTranslator translator = TestUtils.runSemanticTest("qicore/v411/AdultOutpatientEncounters_QICore4-2.0.000.cql", 0);
        Library library = translator.toELM();
        Map<String, ExpressionDef> defs = new HashMap<>();

        if (library.getStatements() != null) {
            for (ExpressionDef def : library.getStatements().getDef()) {
                defs.put(def.getName(), def);
            }
        }

        /*
        ExpressionDef
          expression: Query
            where: And
              operand[0]: IncludedIn
                operand[0]: FunctionRef
                  name: ToInterval
                  libraryName: FHIRHelpers

         */

        ExpressionDef def = defs.get("Qualifying Encounters");
        assertThat(def, notNullValue());
        assertThat(def.getExpression(), instanceOf(Query.class));
        Query query = (Query)def.getExpression();
        assertThat(query.getWhere(), instanceOf(And.class));
        And and = (And)query.getWhere();
        assertThat(and.getOperand().size(), equalTo(2));
        assertThat(and.getOperand().get(0), instanceOf(IncludedIn.class));
        IncludedIn includedIn = (IncludedIn)and.getOperand().get(0);
        assertThat(includedIn.getOperand().size(), equalTo(2));
        assertThat(includedIn.getOperand().get(0), instanceOf(FunctionRef.class));
        FunctionRef functionRef = (FunctionRef)includedIn.getOperand().get(0);
        assertThat(functionRef.getName(), equalTo("ToInterval"));
        assertThat(functionRef.getLibraryName(), equalTo("FHIRHelpers"));
    }

    @Test
    public void testPapTestWithResults() throws IOException {
        CqlTranslator translator = TestUtils.runSemanticTest("qicore/v411/EXM124_QICore4-8.2.000.cql", 0);
        Library library = translator.toELM();
        Map<String, ExpressionDef> defs = new HashMap<>();

        if (library.getStatements() != null) {
            for (ExpressionDef def : library.getStatements().getDef()) {
                defs.put(def.getName(), def);
            }
        }

        ExpressionDef def = defs.get("Pap Test with Results");
        assertThat(def, notNullValue());

        /*
        ExpressionDef: Pap Test with Results
            Query
                Source
                Where: And
                    Operand[0]: Not
                        Operand[0]: IsNull
                            Operand[0]: FunctionRef: FHIRHelpers.ToValue
         */
        assertThat(def.getExpression(), instanceOf(Query.class));
        Query q = (Query)def.getExpression();
        assertThat(q.getWhere(), instanceOf(And.class));
        And a = (And)q.getWhere();
        assertThat(a.getOperand().get(0), instanceOf(Not.class));
        Not n = (Not)a.getOperand().get(0);
        assertThat(n.getOperand(), instanceOf(IsNull.class));
        IsNull i = (IsNull)n.getOperand();
        assertThat(i.getOperand(), instanceOf(FunctionRef.class));
        FunctionRef fr = (FunctionRef)i.getOperand();
        assertThat(fr.getLibraryName(), equalTo("FHIRHelpers"));
        assertThat(fr.getName(), equalTo("ToValue"));
        assertThat(fr.getOperand().get(0), instanceOf(Property.class));
        Property p = (Property)fr.getOperand().get(0);
        assertThat(p.getPath(), equalTo("value"));
        assertThat(p.getScope(), equalTo("PapTest"));
    }
}
