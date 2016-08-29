package bus.endpoints

import bus.api.EndpointAPI
import bus.models.{Endpoint, Resource}
import bus.run.ResourceTypeR
import org.sparx.Repository
import java.io.PrintWriter
import java.io.File

class EaAdapter
  extends EndpointAPI
  with EaMetaData
  with EaCommonFunctions
  with EaConnection {

  var endpoint: Endpoint = null

  def setEndpoint(endpoint: Endpoint): Unit = {
    System.out.println("setting endpoint")
    this.endpoint = endpoint
  }

  /**
    * Executes a query to retrieve the data that should be updated
    * param resourceTypeR
    * param query custom query
    * param incrementFrom date of change. All issues that have been modified after this date will be retrieved by request
    * return list of resources for update
    */
  override def queryResources(resourceTypeR: ResourceTypeR, query: String, incrementFrom: Long): Seq[Resource] = {
    val stereotype = resourceTypeR.resourceTypeRef.resourceTypeName
    val writer = new PrintWriter(new File("C:\\Users\\wdnoc5\\queryResource.txt"))
    val elementList = getInitialElementList(stereotype, incrementFrom)
    writer.write("elements retrieved " + elementList.size)
    writer.close()
    getResourceList(resourceTypeR, elementList, query)
  }

  /**
    * If one of the values included in the custom Request for query Resources changed than the issue would not retrieved
    * This method to get the latest actual issue state
    *
    * param resourceTypeRun
    * param incrementFrom date of change. All issues that have been modified after this date will be retrieved by request
    * param alreadyLoaded map of the already retrieved issues
    * param restrictToIdentifiers
    * return
    */
  override def queryOutOfScopeResources(resourceTypeRun: ResourceTypeR, incrementFrom: Long, alreadyLoaded: Map[String, Resource], restrictToIdentifiers: Seq[String]): List[Resource] = {
    queryResources(resourceTypeRun, "", incrementFrom).toList
  }

  /**
    * Load resource by identifier and use a field set for incoming or outgoing direction (IN/OUT)
    * The incoming direction is for the correct conflict resolution
    * The outgoing direction is for providing data to the bus
    * param resourceTypeR
    * param identifier resource Identifier
    * param direction IN or OUT
    * return founded resource, if it does not exists None
    */
  override def loadResource(resourceTypeR: ResourceTypeR, identifier: String, direction: String): Option[Resource] = {
    require(direction == "OUT" || direction == "IN", "direction must be OUT or IN")
    implicit val resourceTypeR2 = resourceTypeR
    val propertyDefs = if (direction == "OUT") resourceTypeR.outPropertyDefs else resourceTypeR.inPropertyDefs
    val writer = new PrintWriter(new File("C:\\Users\\wdnoc5\\loadResource.txt"))
    writer.write("loading resource: " + identifier + " direction: " + direction)
    writer.close()
    Option(client.GetElementByGuid(identifier)) match {
      case Some(element) =>
        Option(toResource(resourceTypeR, element, propertyDefs))
      case None => None
    }
  }

  /**
    * Write resource to the Jira Endpoint
    * param newResource the incoming (new) resource
    * param oldResourceOpt the old resource before update
    * return new Resource data
    */
  override def writeResource(newResource: Resource, oldResourceOpt: Option[Resource]): Resource = {
    val guid = newResource.identifier
    val elementToUpdate = client.GetElementByGuid(guid)
    val taggedList = elementToUpdate.GetTaggedValues()




    newResource
  }

  /**
    * The method is updating the resource after it has been created or updated
    * @param newResource the new resource to update
    * @param oldResourceOpt the old resource before update
    * @param identifiersNormToEndMap
    * @return new Resource data
    */
  override def writeAfterResourceCreationOrUpdate(newResource: Resource, oldResourceOpt: Option[Resource], identifiersNormToEndMap: Map[String, String]): Resource = {
    val writer = new PrintWriter(new File("C:\\Users\\wdnoc5\\writeAfterResource.txt"))
    writer.write("unexpected method call")
    writer.close()

    newResource
  }

  /**
    * Jira API does not support transactions
    */
  override def txStart(): Unit = {}

  /**
    * Jira API does not support transactions
    */
  override def txRollback(): Unit = {}

  /**
    * Jira API does not support transactions
    */
  override def txCommit(): Unit = {}
}
