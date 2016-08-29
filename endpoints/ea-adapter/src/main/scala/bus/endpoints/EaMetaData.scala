package bus.endpoints

import java.io.{File, PrintWriter}

import bus.models._
import org.sparx.{Collection, Repository, Stereotype}

trait EaMetaData extends EaConnection
{

  def resourceTypeHeaders: Seq[ResourceType] = {
    val stList: Collection[Stereotype] = client.GetStereotypes(); //Collection of stereotypes
    var result = Seq[ResourceType]()

    if(stList.GetCount() > 0)
    {
      for (i <- 0 until stList.GetCount()) {
        val st = stList.GetAt(i.toShort)

        if(st.GetAppliesTo().equals("Requirement"))
          result :+= ResourceType.atEndpoint(endpoint, st.GetName(), label = st.GetName())
      }

      //sort list based on the label in alphabetical order
      result.sortWith(_.label < _.label)
    }
    else
    {
      //client.Exit()
      Seq.empty
    }
  }

  def resourceTypeDetail(resourceTypeName: String): Seq[PropertyDef] = {
    val mylist = prepPropertyDefs
    val writer = new PrintWriter(new File("C:\\Users\\wdnoc5\\detailCheck.txt"))

    for(i <- 0 until mylist.size)
    {
      val prop = mylist(i)
      writer.write("Name: " + prop.name + " Label: " + prop.label)
      writer.println()
      writer.flush()
    }

    writer.close()
    mylist
  }

  //called by resourceTypeDetail
  def prepPropertyDefs: Seq[PropertyDef] = {
    val fields = EaStandardProperties.standardPropertyList ++ getCustomFieldsForType
    prepPropertyDefs(fields)
  }

  /**
    * Preparation of metadata for properties - fields in structure understandable for the platform
    * @param fields  input list of fields
    * @return PropertyRef list
    */
  def prepPropertyDefs(fields: Seq[PropertyDef]): Seq[PropertyDef] = {
    var fields2 = Seq[PropertyDef]()

    for(i <- fields.indices)
    {
      val prop = fields(i)

      val enumValues = prop.name match
      {
        case "difficulty" => getEnumList("List:DifficultyType")
        case "priority" => getEnumList("List:PriorityType")
        case "status" => getEnumList("Status")
        case _ => prop.enumValueDefs
      }

      fields2 :+= PropertyDef(prop.name, prop.dataType, prop.identifier, prop.normalize, prop.computed, prop.canWrite,
        prop.canRead, prop.label, enumValueDefs = enumValues)
    }

    fields2.map(pd => pd.copy(label = pd.labelCalculated))
  }

  //get enumerated list of values for system defined attributes
  def getEnumList(fieldName: String): Seq[EnumValueDef] = {
    val enumList = client.GetReferenceList(fieldName)
    var newList = Seq[EnumValueDef]()

    //go through each value in the enumerated list and append to the new list
    for(j <- 0 until enumList.GetCount())
    {
      newList :+= new EnumValueDef(enumList.GetAt(j.toShort), enumList.GetAt(j.toShort))
    }

    newList
  }

  //get list of EA Tagged Values. Tagged Values are only available as string types
  def getCustomFieldsForType: Seq[PropertyDef] = {
    val allFields = client.GetPropertyTypes();  //returns a Collection<PropertyType> of EA Tags
    var customFields = Seq[PropertyDef]()

    for(i <- 0 until allFields.GetCount())
    {
        val prop = allFields.GetAt(i.toShort) //get a single PropertyType from the collection
        customFields :+= PropertyDef(prop.GetTag(), dataType = PropType.STRING, canWrite = true, canRead = true,
          label = prop.GetTag(), enumValueDefs = Seq[EnumValueDef]())
    }

    customFields
  }
}
