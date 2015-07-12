/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.kuleuven.esat.fsscala.prototype

import breeze.linalg.DenseVector
import be.kuleuven.esat.fsscala.kernels.DensityKernel
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
 * Models a general entropy measure.
 * Any entropy measure would require a
 * probability distribution
 */
abstract class EntropyMeasure extends Measure[DenseVector[Double]] with Serializable {

  protected val density: DensityKernel

  /**
   * Given a probability distribution for
   * the data set, calculate the entropy of
   * the data set with respect to the given
   * distribution.
   *
   * @param data The data set whose entropy is
   *             required.
   *
   * @return The entropy of the data set.
   * */

  def entropy(data: List[DenseVector[Double]]): Double

  override def evaluate(data: List[DenseVector[Double]]): Double = this.entropy(data)

  def entropy[K](data: RDD[(K, LabeledPoint)]): Double

  def evaluate[K](data: RDD[(K, LabeledPoint)]): Double = this.entropy[K](data)
}
