package ca.uhn.fhir.jpa.provider;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.hl7.fhir.instance.model.api.IIdType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;

public class ResourceProviderDstu2ValueSetTest extends BaseResourceProviderDstu2Test {

	private IIdType myExtensionalVsId;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ResourceProviderDstu2ValueSetTest.class);
	
	@Before
	@Transactional
	public void before02() throws IOException {
		ValueSet upload = loadResourceFromClasspath(ValueSet.class, "/extensional-case-2.xml");
		upload.setId("");
		myExtensionalVsId = myValueSetDao.create(upload).getId().toUnqualifiedVersionless();
	}
	
	@Test
	public void testExpandById() throws IOException {
		//@formatter:off
		Parameters respParam = ourClient
			.operation()
			.onInstance(myExtensionalVsId)
			.named("expand")
			.withNoParameters(Parameters.class)
			.execute();
		ValueSet expanded = (ValueSet) respParam.getParameter().get(0).getResource();
		//@formatter:on
		
		String resp = myFhirCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(expanded);
		ourLog.info(resp);
		// @formatter:off
		assertThat(resp,
			stringContainsInOrder("<ValueSet xmlns=\"http://hl7.org/fhir\">", 
				"<expansion>", 
					"<contains>", 
						"<system value=\"http://loinc.org\"/>",
						"<code value=\"11378-7\"/>",
						"<display value=\"Systolic blood pressure at First encounter\"/>", 
					"</contains>",
					"<contains>", 
						"<system value=\"http://loinc.org\"/>",
						"<code value=\"8450-9\"/>", 
						"<display value=\"Systolic blood pressure--expiration\"/>", 
					"</contains>",
				"</expansion>" 
					));
		//@formatter:on

		/*
		 * Filter with display name
		 */

		//@formatter:off
		respParam = ourClient
			.operation()
			.onInstance(myExtensionalVsId)
			.named("expand")
			.withParameter(Parameters.class, "filter", new StringDt("systolic"))
			.execute();
		expanded = (ValueSet) respParam.getParameter().get(0).getResource();
		//@formatter:on

		expanded = myValueSetDao.expand(myExtensionalVsId, ("systolic"));
		resp = myFhirCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(expanded);
		ourLog.info(resp);
		//@formatter:off
		assertThat(resp, stringContainsInOrder(
				"<code value=\"11378-7\"/>", 
				"<display value=\"Systolic blood pressure at First encounter\"/>"));
		//@formatter:on

		/*
		 * Filter with code
		 */

		//@formatter:off
		respParam = ourClient
			.operation()
			.onInstance(myExtensionalVsId)
			.named("expand")
			.withParameter(Parameters.class, "filter", new StringDt("11378"))
			.execute();
		expanded = (ValueSet) respParam.getParameter().get(0).getResource();
		//@formatter:on
		resp = myFhirCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(expanded);
		ourLog.info(resp);
		//@formatter:off
		assertThat(resp, stringContainsInOrder(
				"<code value=\"11378-7\"/>", 
				"<display value=\"Systolic blood pressure at First encounter\"/>"));
		//@formatter:on
	}
	
	@Test
	public void testExpandByIdentifier() {
		//@formatter:off
		Parameters respParam = ourClient
			.operation()
			.onInstance(myExtensionalVsId)
			.named("expand")
			.withParameter(Parameters.class, "identifier", new UriDt("http://www.healthintersections.com.au/fhir/ValueSet/extensional-case-2"))
			.andParameter("filter", new StringDt("11378"))
			.execute();
		ValueSet expanded = (ValueSet) respParam.getParameter().get(0).getResource();
		//@formatter:on

		String resp = myFhirCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(expanded);
		ourLog.info(resp);
		//@formatter:off
		assertThat(resp, stringContainsInOrder(
				"<code value=\"11378-7\"/>", 
				"<display value=\"Systolic blood pressure at First encounter\"/>"));
		//@formatter:on

		assertThat(resp, not(containsString("<code value=\"8450-9\"/>")));
	}

	@Test
	public void testExpandByValueSet() throws IOException {
		ValueSet toExpand = loadResourceFromClasspath(ValueSet.class, "/extensional-case-2.xml");
		
		//@formatter:off
		Parameters respParam = ourClient
			.operation()
			.onInstance(myExtensionalVsId)
			.named("expand")
			.withParameter(Parameters.class, "valueSet", toExpand)
			.andParameter("filter", new StringDt("11378"))
			.execute();
		ValueSet expanded = (ValueSet) respParam.getParameter().get(0).getResource();
		//@formatter:on

		String resp = myFhirCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(expanded);
		ourLog.info(resp);
		//@formatter:off
		assertThat(resp, stringContainsInOrder(
				"<code value=\"11378-7\"/>", 
				"<display value=\"Systolic blood pressure at First encounter\"/>"));
		//@formatter:on

		assertThat(resp, not(containsString("<code value=\"8450-9\"/>")));
	}

	
}
