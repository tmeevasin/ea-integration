import bus.dsl.DslConfiguration
import bus.endpoints2.PolarionAdapter
import bus.models.EndpointAuth

object MyConfig extends DslConfiguration {

  val auth = EndpointAuth("admin", "admin")

  polarionWS("polarion1", "PolarionTEST", "http://localhost/", auth) {
    project =>
      project.resourceType("task") {
        rt =>
          PolarionAdapter.standardPropertyList.foreach(rt.propertyDef)
      }
  }

  polarionWS("polarion2", "PolarionTEST2", "http://localhost/", auth) {
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
