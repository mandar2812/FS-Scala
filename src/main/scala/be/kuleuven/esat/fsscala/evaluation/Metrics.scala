package be.kuleuven.esat.fsscala.evaluation

import breeze.linalg.DenseVector
import org.apache.spark.rdd.RDD

/**
 * Abstract trait for metrics
 */
trait Metrics[P] {
  protected val scoresAndLabels: List[(P, P)]
  def print(): Unit
  def generatePlots(): Unit = {}
  def kpi(): DenseVector[P]
}

object Metrics{

}

object MetricsSpark {
  def apply(task: String)
           (scoresAndLabels: RDD[(Double, Double)], length: Long)
  : Metrics[Double] = task match {
    case "regression" => new RegressionMetricsSpark(scoresAndLabels, length)
    case "classification" => new BinaryClassificationMetricsSpark(scoresAndLabels, length)
  }
}
