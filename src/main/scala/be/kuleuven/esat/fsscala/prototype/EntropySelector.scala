package be.kuleuven.esat.fsscala.prototype

import be.kuleuven.esat.fsscala.models.svm.KernelSparkModel
import breeze.linalg.DenseVector
import org.apache.log4j.{Logger, Priority}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
 * Basic skeleton of an entropy based
 * subset selector
 */
abstract class EntropySelector
  extends SubsetSelector[KernelSparkModel, DenseVector[Double]]
  with Serializable {
  protected val measure: EntropyMeasure
  protected val delta: Double
  protected val MAX_ITERATIONS: Int
}

object GreedyEntropySelector {
  private val logger = Logger.getLogger(this.getClass)


  def subsetSelection(data: RDD[(Long, LabeledPoint)],
                      measure: EntropyMeasure, M: Int,
                      MAX_ITERATIONS: Int,
                      delta: Double): RDD[(Long, LabeledPoint)] = {

    /*
    * Draw an initial sample of M points
    * from data without replacement.
    *
    * Define a working set which we
    * will use as a prototype set to
    * to each iteration
    * */
    logger.info("Initializing the working set, by drawing randomly from the training set")
    val workingset = data.keys.takeSample(false, M)

    val r = scala.util.Random
    var it: Int = 0

    // All the elements not in the working set
    var newDataset: RDD[Long] = data.keys.filter((p) => !workingset.contains(p))
    // Existing best value of the entropy
    var oldEntropy: Double = measure.evaluate(data.filter((point) =>
      workingset.contains(point._1)))
    // Store the value of entropy after an element swap
    var newEntropy: Double = 0.0
    var d: Double = Double.NegativeInfinity
    var rand: Int = 0
    logger.info("Starting iterative, entropy based greedy subset selection")
    do {
      /*
       * Randomly select a point from
       * the working set as well as data
       * and then swap them.
       * */
      rand = r.nextInt(workingset.length - 1)
      val point1 = workingset.apply(rand)

      val point2 = newDataset.takeSample(false, 1).apply(0)

      // Update the working set
      workingset(rand) = point2
      // Calculate the new entropy
      newEntropy = measure.evaluate(data.filter((p) =>
        workingset.contains(p._1)))

      /*
      * Calculate the change in entropy,
      * if it has improved then keep the
      * swap, otherwise revert to existing
      * working set.
      * */
      d = newEntropy - oldEntropy

      if(d > 0) {
        /*
        * Improvement in entropy so
        * keep the updated working set
        * as it is and update the
        * variable 'newDataset'
        * */
        it += 1
        oldEntropy = newEntropy
        newDataset = data.keys.filter((p) => !workingset.contains(p))
      } else {
        /*
        * No improvement in entropy
        * so revert the working set
        * to its initial state. Leave
        * the variable newDataset as
        * it is.
        * */
        workingset(rand) = point1
      }

    } while(math.abs(d) >= delta &&
      it <= MAX_ITERATIONS)
    logger.info("Working set obtained, now starting process of packaging it as an RDD")
    // Time to return the final working set
    data.filter((p) => workingset.contains(p._1))
  }
}
