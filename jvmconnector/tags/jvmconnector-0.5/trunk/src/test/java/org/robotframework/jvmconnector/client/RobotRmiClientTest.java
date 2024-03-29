/*
 * Copyright 2008 Nokia Siemens Networks Oyj
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robotframework.jvmconnector.client;

import java.io.ByteArrayOutputStream;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.robotframework.jvmconnector.client.RobotRmiClient;
import org.robotframework.jvmconnector.common.KeywordExecutionResult;
import org.robotframework.jvmconnector.common.TestFailedException;
import org.robotframework.jvmconnector.server.RobotRmiService;
import org.robotframework.jvmconnector.util.LoggerUtil;
import org.springframework.beans.factory.BeanFactory;


public class RobotRmiClientTest extends MockObjectTestCase {
	private Mock mockRobotRmiService;
	private Mock mockBeanFactory;
	private Mock mockKeywordExecutionResults;
	private KeywordExecutionResult keywordExecutionResults;
	private RobotRmiClient rmiClient;
	private BeanFactory beanFactoryContainingRmiService;
	private String propertiesSetterMethodName = "setLibraryProperties";

	public void setUp() {
		setMockBeanFactory();
		setMockKeywordExecutionResults();
		setMockRobotRmiService();
		setBeanFactoryContainingRmiService();
		rmiClient = new RobotRmiClient(beanFactoryContainingRmiService);
	}
	
	public void tearDown() {
		LoggerUtil.resetSystemOut();
		LoggerUtil.resetSystemErr();
	}

	public void testUsesBeanFactoryInConstructorForFetchingRobotRmiService() {
		mockBeanFactory.expects(once()).method("getBean")
			.with(eq("robotRmiService"))
			.will(returnValue((RobotRmiService)mockRobotRmiService.proxy()));
		new RobotRmiClient(beanFactoryContainingRmiService);
	}
	
	public void testUsesRmiServiceForFindingKeywords() {
		String[] keywordNames = new String[] { "Keyword Name" };
		mockRobotRmiService.expects(once()).method("getKeywordNames")
			.will(returnValue(keywordNames));
		
		assertEquals(keywordNames, rmiClient.getKeywordNames());
	}
	
	public void testUsesRmiServiceForRunningKeyword() {
		String keywordName = "Fake Keyword";
		Object[] args = new Object[0];
		
		mockRobotRmiService.expects(once()).method("runKeyword")
			.with(eq(keywordName), eq(args))
			.will(returnValue(keywordExecutionResults));

		rmiClient.runKeyword(keywordName, args);
	}
	
	public void testReturnsResultsFromKeywordExecutionResults() {
		Object keywordReturnValue = new Object();
		
		mockKeywordExecutionResults.expects(once()).method("getResult")
			.will(returnValue(keywordReturnValue));
		
		assertEquals(keywordReturnValue, rmiClient.runKeyword(null, null));
	}
	
	public void testKeywordExecutionResultsStdOutIsPrintedWithSystemOut() throws Exception {
		ByteArrayOutputStream redirectedSystemOut = LoggerUtil.redirectSystemOut();
		assertStreamBufferContainsLogOutput(redirectedSystemOut, "getStdOutAsString");
	}
	
	public void testKeywordExecutionResultsStdErrIsPrintedWithSystemErr() throws Exception {
		ByteArrayOutputStream redirectedSystemErr = LoggerUtil.redirectSystemErr();
		assertStreamBufferContainsLogOutput(redirectedSystemErr, "getStdErrAsString");
	}
	
	public void testChecksIfKeywordHasPassed() {
	    mockKeywordExecutionResults.expects(once()).method("isKeywordPassed")
	    	.will(returnValue(true));
		rmiClient.runKeyword(null, null);
	}
	
	public void testThrowsTestFailedExceptionIfTestFailed() {
	    String isKeywordPassedInvocation = "passedTest";
		mockKeywordExecutionResults.expects(once()).method("isKeywordPassed")
	    	.will(returnValue(false))
	    	.id(isKeywordPassedInvocation);
		
	    mockKeywordExecutionResults.expects(once()).method("getTestFailedException")
	    	.after(isKeywordPassedInvocation)
	    	.will(returnValue(new TestFailedException(new RuntimeException())));
	    
		try {
			rmiClient.runKeyword(null, null);
		} catch (TestFailedException e) {
			return;
		}
		
		fail("expected TestFailedException to be thrown");
	}
	
	private void assertStreamBufferContainsLogOutput(ByteArrayOutputStream stdStream, String stdStreamGetterName) {
		String expectedLogValue = "Something";
		
		mockKeywordExecutionResults.expects(once()).method(stdStreamGetterName)
			.will(returnValue(expectedLogValue));
		
		rmiClient.runKeyword(null, null);
		assertEquals(expectedLogValue, stdStream.toString());
	}
	
	public void testGivenClasspathPatternIsSetToRmiService() {
		String pattern = "some classpath pattern";
		mockRobotRmiService.expects(once()).method(propertiesSetterMethodName)
			.with(eq(pattern));
		
		new RobotRmiClient((BeanFactory) mockBeanFactory.proxy(), pattern);
	}
	
	public void testNullClasspathPatternsAreNotSetToRmiService() {
		mockRobotRmiService.expects(never()).method("setLibraryProperties");
		
		new RobotRmiClient((BeanFactory) mockBeanFactory.proxy(), null);		
	}
	
	private void setBeanFactoryContainingRmiService() {
		mockBeanFactory.stubs().method("getBean")
			.with(eq("robotRmiService"))
			.will(returnValue((RobotRmiService)mockRobotRmiService.proxy()));
		beanFactoryContainingRmiService = (BeanFactory) mockBeanFactory.proxy();
	}
	
	private void setMockKeywordExecutionResults() {
		mockKeywordExecutionResults = mock(KeywordExecutionResult.class);
		mockKeywordExecutionResults.stubs();
		mockKeywordExecutionResults.stubs().method("isKeywordPassed").will(returnValue(true));
		mockKeywordExecutionResults.stubs().method("getStdOutAsString").will(returnValue(""));
		mockKeywordExecutionResults.stubs().method("getStdErrAsString").will(returnValue(""));
		keywordExecutionResults = (KeywordExecutionResult)mockKeywordExecutionResults.proxy();
	}
	
	private void setMockRobotRmiService() {
		mockRobotRmiService = mock(RobotRmiService.class);
		mockRobotRmiService.stubs().method(propertiesSetterMethodName);
		mockRobotRmiService.stubs().method("runKeyword")
			.will(returnValue(keywordExecutionResults));
	}
	
	private void setMockBeanFactory() {
		mockBeanFactory = mock(BeanFactory.class);
	}
}
