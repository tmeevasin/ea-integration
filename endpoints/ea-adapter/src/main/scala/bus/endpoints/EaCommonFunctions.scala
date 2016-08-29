package bus.endpoints

import java.io.{File, PrintWriter}
import java.util.Date

import bus.models.{PropertyDef, Resource}
import bus.run.ResourceTypeR
import org.sparx._

trait EaCommonFunctions extends EaConnection{

  def traverseModels(): Seq[Element] =
  {
    var elemList: Seq[Element] = Seq[Element]()

    for(i <- 0 until client.GetModels().GetCount())
    {
      elemList = traversePackages(client.GetModels().GetAt(i.toShort), elemList)
    }

    elemList
  }

  //output package name, then element contents, then process child packages
  def traversePackages(p: Package, elemList: Seq[Element]): Seq[Element] =
  {
    var updatedList = elemList
    //traverse sub-packages
    for(i <- 0 until p.GetPackages().GetCount())
    {
      updatedList = traversePackages(p.GetPackages().GetAt(i.toShort), updatedList)
    }

    //traverse elements
    for(j <- 0 until p.GetElements().GetCount())
    {
      updatedList = traverseElements(p.GetElements().GetAt(j.toShort), updatedList)
    }

    updatedList
  }

  def traverseElements(e: Element, elemList: Seq[Element]): Seq[Element] =
  {
    var updatedList = elemList

    updatedList :+= e
    //traverse sub-elements
    for(k <- 0 until e.GetElements().GetCount())
    {
      updatedList = traverseElements(e.GetElements().GetAt(k.toShort), updatedList)
    }

    updatedList
  }

  def getInitialElementList(stereotype: String, incrementFrom: Long = 0): Seq[Element] =
  {
    val writer = new PrintWriter(new File("C:\\Users\\wdnoc5\\elementCheck.txt"))
    var queryResult = Seq[Element]()
    var baseQuery: Seq[Element] = Seq[Element]()
    val incrementDate = new Date(incrementFrom)

    baseQuery = traverseModels()
    writer.write("number of element: " + baseQuery.size)
    writer.flush()
    for(i <- 0 until baseQuery.size)
    {
      val e: Element = baseQuery(i)  //get each Element
      if(e.GetStereotype().equals(stereotype) && e.GetModified().after(incrementDate))
      {
        queryResult :+= e
      }
    }
writer.close()
    queryResult
  }

  def toResource(resourceTypeR: ResourceTypeR, e: Element, propertyDefs: Seq[PropertyDef]): Resource = {
    val writer = new PrintWriter(new File("C:\\Users\\wdnoc5\\resourceCheck.txt"))
    val mandatoryProps = Map(
      "uri" -> e.GetElementGUID(),  //no uri so we'll use the GUID instead
      "id" -> e.GetElementGUID(),
      "projectName" -> Option(client.GetPackageByID(e.GetPackageID()).GetName()).getOrElse(""),
      "type" -> Option(e.GetStereotype()).getOrElse(""))

    val props = Map(
      "alias" -> Some(e.GetAlias()),
      "difficulty" -> Some(e.GetDifficulty()),
      "name" -> Some(e.GetName()),
      "notes" -> Some(e.GetNotes()),
      "phase" -> Some(e.GetPhase()),
      "priority" -> Some(e.GetPriority()),
      "status" -> Some(e.GetStatus()),
      "tag" -> Some(e.GetTag()),
      "version" -> Some(e.GetVersion()))
    writer.write("found the element: " + e.GetName())
    writer.close()
    var customProp = Map[String, Option[Any]]()//e.GetTaggedValues()
    val customList = e.GetTaggedValues()

    for(i <- 0 until customList.GetCount())
    {
      val tv: TaggedValue = customList.GetAt(i.toShort)

      if(tv.GetValue().equals("<memo>"))
        customProp += tv.GetName() -> Some(tv.GetNotes())
      else
        customProp += tv.GetName() -> Some(tv.GetValue())
    }

    new Resource(
      resourceTypeR,
      properties = filterOnlyDefsProperty(mandatoryProps ++ props ++ customProp, propertyDefs),
      changeOrigResourceTypeRun = resourceTypeR,
      updatedAtEndpoint = e.GetModified().getTime()
    )
  }

  def filterOnlyDefsProperty(props: Map[String, Any], propertyDefs: Seq[PropertyDef], predicate: PropertyDef => Boolean = { p => true }): Map[String, Any] = {
    propertyDefs
      .filter(p => props.contains(p.name) && predicate(p))
      .map(p => p.name -> props.getOrElse(p.name, ""))
      .toMap
  }

  def getResourceList(resourceTypeR: ResourceTypeR, elementList: Seq[Element], query: String = ""): Seq[Resource] =
  {
    var resourceList = Seq[Resource]()

    //execute query here

    for(i <- 0 until elementList.size)
    {
      val e: Element = elementList(i)

      resourceList :+= toResource(resourceTypeR, e, resourceTypeR.outPropertyDefs)
    }

    resourceList
  }
}
