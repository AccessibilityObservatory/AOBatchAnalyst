import scala.collection.JavaConversions._
import edu.umn.ao.ao_batch_analyst.BatchProcessor
import edu.umn.ao.ao_batch_analyst.MultipleAttributeShapefilePopulation
import edu.umn.ao.ao_batch_analyst.IndividualRoutingRequest
import edu.umn.ao.ao_batch_analyst.IndividualRoutingRequestFactory
import org.opentripplanner.routing.core.TraverseModeSet
import org.opentripplanner.routing.impl.GraphServiceImpl
import java.util.GregorianCalendar

val originFilename = "/Users/owenx148/Desktop/temp/33460-27053-20140122/test.shp"
val destinationFilename = "/Users/owenx148/Desktop/temp/33460-27053-20140122/destinations.shp"
val label = "GEOID10"
val originValueAttributes = List()
val destValueAttributes = List("JOBS2011")

val origins = new MultipleAttributeShapefilePopulation(originFilename, label, originValueAttributes)
val destinations = new MultipleAttributeShapefilePopulation(destinationFilename, label, destValueAttributes)

val gs = new GraphServiceImpl()
gs.setPath("/Users/owenx148/Desktop/temp")
gs.setDefaultRouterId("33460-27053-20140122")
gs.registerGraph("33460-27053-20140122", true)

val modes = new TraverseModeSet()
modes.setWalk(true)
modes.setTransit(true)

val protoReq = new IndividualRoutingRequest()
protoReq.setModes(modes)
protoReq.setWalkSpeed(1.388888)
protoReq.setStairsReluctance(1.0)
protoReq.setWalkReluctance(1.0)
protoReq.setWaitReluctance(1.0)
protoReq.setWaitAtBeginningFactor(1.0)
protoReq.setClampInitialWait(0)
protoReq.setArriveBy(false)
protoReq.setWalkBoardCost(0)
protoReq.setMaxWalkDistance(5000)
protoReq.setBatch(true)

val reqFactory = new IndividualRoutingRequestFactory(gs, protoReq)

val dates = List(new GregorianCalendar(2014, 1, 22, 8, 0).getTime())
val thresholds = List(300,600) 
implicit def toIntegerList( lst: List[Int] ) = seqAsJavaList( lst.map( i => i:java.lang.Integer ) )

val bp = new BatchProcessor(gs, origins, destinations, reqFactory)
bp.setDepTimes(dates)
bp.setThresholds(List(300,600))
bp.run()
