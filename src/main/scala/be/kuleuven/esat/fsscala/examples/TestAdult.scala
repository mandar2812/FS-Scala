package be.kuleuven.esat.fsscala.examples

import java.io.File

import breeze.linalg.{DenseMatrix, DenseVector}
import com.github.tototoshi.csv.CSVWriter
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import be.kuleuven.esat.fsscala.kernels.{RBFKernel, SVMKernel}
import be.kuleuven.esat.fsscala.models.KernelizedModel
import be.kuleuven.esat.fsscala.models.svm.{KernelSparkModel, LSSVMSparkModel}

/**
 * @author mandar2812 on 1/7/15.
 */
object TestAdult {
  def apply(nCores: Int = 4, prototypes: Int = 1, kernel: String,
            globalOptMethod: String = "gs", grid: Int = 7,
            step: Double = 0.3, logscale: Boolean = false, frac: Double): Unit = {

    val dataRoot = "data/" //"/esat/smcdata/guests/mandar/"
    val trainfile = dataRoot+"adult.csv"
    val testfile = dataRoot+"adulttest.csv"

    val config = Map(
      "file" -> trainfile,
      "delim" -> ",",
      "head" -> "false",
      "task" -> "classification",
      "parallelism" -> nCores.toString
    )

    val configtest = Map("file" -> testfile,
      "delim" -> ",",
      "head" -> "false")

    val conf = new SparkConf().setAppName("Adult").setMaster("local["+nCores+"]")

    conf.registerKryoClasses(Array(classOf[LSSVMSparkModel], classOf[KernelSparkModel],
      classOf[KernelizedModel[RDD[(Long, LabeledPoint)], RDD[LabeledPoint],
        DenseVector[Double], DenseVector[Double], Double, Int, Int]],
      classOf[SVMKernel[DenseMatrix[Double]]], classOf[RBFKernel],
      classOf[DenseVector[Double]],
      classOf[DenseMatrix[Double]]))

    val sc = new SparkContext(conf)

    val model = LSSVMSparkModel(config, sc)

    val nProt = if (kernel == "Linear") {
      model.npoints.toInt
    } else {
      if(prototypes > 0)
        prototypes
      else
        math.sqrt(model.npoints.toDouble).toInt
    }

    model.setBatchFraction(frac)
    val (optModel, optConfig) = KernelizedModel.getOptimizedModel[RDD[(Long, LabeledPoint)],
      RDD[LabeledPoint], model.type](model, globalOptMethod,
        kernel, nProt, grid, step, logscale)

    optModel.setMaxIterations(2).learn()

    val met = optModel.evaluate(configtest)



    met.print()
    println("Optimal Configuration: "+optConfig)
    val scale = if(logscale) "log" else "linear"

    val perf = met.kpi()
    val row = Seq(kernel, prototypes.toString, globalOptMethod,
      grid.toString, step.toString, scale,
      perf(0), perf(1), perf(2), optConfig.toString)

    val writer = CSVWriter.open(new File("data/resultsAdult.csv"), append = true)
    writer.writeRow(row)
    writer.close()
    optModel.unpersist
  }
}
