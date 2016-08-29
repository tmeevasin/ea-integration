import bus.dsl.DslConfiguration
import bus.endpoints2.PolarionAdapter
import bus.models.EndpointAuth

object Config1 extends DslConfiguration {

  val auth = EndpointAuth("integ", "integ1")

  polarionWS("polarion1", "ProjectA10", "http://p1.versative.com", auth) {
    project =>
      project.resourceType("task") {
        rt =>
          PolarionAdapter.standardPropertyList.foreach(rt.propertyDef)
      }
  }

  polarionWS("polarion2", "ProjectB10", "http://p2.versative.com", auth) {
    project =>
      project.resourceType("task") {
        rt =>
          PolarionAdapter.standardPropertyList.foreach(rt.propertyDef)
      }
  }

  normalized("task2") {
    n =>
      PolarionAdapter.standardPropertyList.foreach(n.propertyDef)
      n.property("custom1")

      n.mappingPolarion("polarion1", "ProjectA10","task") {
        m =>
          PolarionAdapter.standardPropertyList.foreach{
            p => m.bind(normPropertyName = p.name, propertyName = p.name)
          }
      }

      n.mappingPolarion("polarion2", "ProjectB10","task") {
        m =>
          PolarionAdapter.standardPropertyList.foreach{
            p => m.bind(normPropertyName = p.name, propertyName = p.name)
          }
      }

  }
}
