package bus.endpoints

import java.io.IOException

import bus.models.Endpoint
import org.sparx.{Package, Repository}

//import java.io.PrintWriter;
//import java.io.File;
trait EaConnection
{
  def endpoint: Endpoint
  private var clientCached:Repository  = null
System.out.println("inside connection")
  def client: Repository =
  {
    try
    {
      if (clientCached != null && clientCached.GetInstanceGUID() != null)
      {
        System.out.println("cached")
        clientCached
      }
      else
      {
        if(clientCached == null)
          System.out.println("null cache")
        else
          System.out.println("GUID: " + clientCached.GetInstanceGUID())
        val connectionString = endpoint.projectName + " --- DBType=0;Connect=Provider=MSDASQL.1;Persist Security Info=False;Data Source=" + endpoint.url + ";LazyLoad=1;"
        var myRepo: Repository = null
        myRepo = new Repository
        myRepo.OpenFile(connectionString)
        clientCached = myRepo
        myRepo
      }
    }
    catch
    {
      case ex: Exception => throw ex
    }
  }

  /**
    * Checks the connection.
    * The method is called from UI to validate correct configuration data for an endpoint
    *
    * @return true if the connection is established, false if the connection is not established
    */
  def checkEndpointConnection(): Boolean =
  {
    var sessionId: String = null

    if(client == null)
      false

    sessionId = client.GetInstanceGUID
    if (sessionId == null || sessionId.length == 0)
      false
    else
      true
  }

  /**
    * Check the existence of the project
    *
    * @param projectName checked project name
    * @return true if the project exists, false if the project does not exist
    */
  def checkProject(projectName: String): Boolean =
  {
    var i: Int = 0
    while (i < client.GetModels.GetCount)
    {
      val p: Package = client.GetModels.GetAt(i.toShort)
      val packageName: String = p.GetName

      if (packageName == projectName)
      {
        return true
      }

      i += 1
    }

    return false
  }
}