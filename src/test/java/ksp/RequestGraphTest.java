package ksp;

import java.util.ArrayList;
import org.junit.Rule;
import org.junit.Test;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;

public class RequestGraphTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  RequestGraph requestGraph =  new RequestGraph(new ArrayList<>());

  @Test
  public void addRequestTest(){
    System.out.println(requestGraph);
    //requestGraph.addNewRequest();
  }
}