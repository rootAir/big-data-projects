import org.apache.spark.SparkConf
import org.apache.spark.streaming.{StreamingContext, Seconds}
import org.apache.spark.streaming.flume._
/**
  * Created by Karthik Ramakrishnan on 12/30/17.
  */
object StreamingDeptCountFlume {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("flume streaming dept count")
      .setMaster(args(0))

    val ssc = new StreamingContext(conf, Seconds(30))

    val stream  = FlumeUtils.createPollingStream(ssc, args(1), args(2).toInt)

    val messages = stream.map(st => {
      new String(st.event.getBody.array())
    })

    val departmentMessages = messages.
      filter(msg => {
        val endpoint = msg.split(" ")(6)
        endpoint.split("/")(1) == "department"
      })

    val departments = departmentMessages.
      map(dm => {
        val endpoint = dm.split(" ")(6)
        val departmentName = endpoint.split("/")(2)
        (departmentName, 1)
      })

    val departmentTraffic = departments.reduceByKey(_+_)

    departmentTraffic.saveAsTextFiles("/user/karthikramakrishnan20/sdcflume/cnt")

    ssc.start()
    ssc.awaitTermination()
  }
}
