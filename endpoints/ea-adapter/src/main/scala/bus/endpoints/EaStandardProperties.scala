package bus.endpoints

import bus.models.{PropType, PropertyDef}

object EaStandardProperties {
  val standardPropertyList = List(
    //mandatory fields
    PropertyDef("id", normalize = false, canWrite = false, canRead = true, label = "GUID"), //true
    PropertyDef("projectName", normalize = false, dataType = PropType.PROJECT_NAME, canWrite = false, canRead = true, label = "Project Name"),
    PropertyDef("uri", identifier = true, normalize = false, canWrite = false, canRead = true, label = "URI"),
    PropertyDef("alias", canWrite = true, canRead = true, label = "Alias"),
    PropertyDef("difficulty", dataType = PropType.ENUM, canWrite = true, canRead = true, label = "Difficulty"),
    PropertyDef("name", canWrite = true, canRead = true, label = "Name"),
    PropertyDef("notes", dataType = PropType.TEXT, canWrite = true, canRead = true, label = "Notes"),
    PropertyDef("phase", canWrite = true, canRead = true, label = "Phase"),
    PropertyDef("priority", dataType = PropType.ENUM, canWrite = true, canRead = true, label = "Priority"),
    PropertyDef("status", dataType = PropType.ENUM, canWrite = true, canRead = true, label = "Status"),
    PropertyDef("tag", canWrite = false, canRead = true, label = "Keywords"),  //this is keywords in EA interfaces
    PropertyDef("version", canWrite = true, canRead = true, label = "Version")
  )
}
