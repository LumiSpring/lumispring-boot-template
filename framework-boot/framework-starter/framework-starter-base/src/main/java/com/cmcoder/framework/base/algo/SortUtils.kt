package com.lumispring.framework.base.algo

import com.lumispring.framework.base.extension.createList
import com.lumispring.framework.base.extension.randomInt
import com.lumispring.framework.base.extension.toJsonString

class SortUtils {
    companion object {
        /**
         * 冒泡排序：
         * 冒泡排序（Bubble Sort）的核心思想非常简单：从头开始，不断比较相邻的两个元素，如果前者比后者大，就交换位置。
         * 这样从头到尾完整地走一趟，最大的那个元素就必然会“沉”到队尾的最终位置。
         * 只需不断重复这个过程，每一轮都将当前未排序部分的最大值找出来送到末端，直到所有元素都排列整齐。
         * 因为在这个过程中较小的元素会像气泡一样逐渐“冒”到数组的前方，所以叫冒泡排序故得此名。
         */
        fun bubbleSort(array: IntArray) :IntArray{
            // 一共比较 n - 1 轮
            for (i in 0 until array.size - 1) {
                // 每轮比较 n - 1 - i次
                for (j in 0 until array.size - 1 - i) {
                    // 如果前面大于后面，交换位置
                    if (array[j] > array[j + 1]) {
                        val temp = array[j]
                        array[j] = array[j + 1]
                        array[j + 1] = temp
                    }
                }
                println("第${i+1}轮排序后：${array.toJsonString()}")
            }
            return array
        }

        /**
         * 选择排序
         * 选择排序是一种非常简单直观的排序算法，工作原理可以概括为“每次从未排序的队伍中，选出最优者，让它归位”。
         * 具体来说，算法在每一轮都会遍历所有还未排序的元素，从中找出最小（或最大）的一个，然后将其与未排序部分的第一个元素交换位置。
         * 这个操作能确保每一轮过后，都有一个元素被精准地放在它最终的正确位置上。
         * 接着，算法会缩小范围，在剩下的元素中重复这个“选择与交换”的过程，直到整个序列完全有序。
         */
        fun selectSort(array: IntArray) :IntArray {
            for (i in 0 until array.size - 1) {
                // 每一轮都要找到最小值的下标
                var minIndex = i
                for (j in i + 1 until array.size) {
                    // 如果找到更小的，标记一下
                    if (array[j] < array[minIndex]) {
                        minIndex = j
                    }
                }
                // 如果最小的下标不等于初始化时的下标，则需要替换
                if (minIndex != i) {
                    val temp = array[minIndex]
                    array[minIndex] = array[i]
                    array[i] = temp
                }
                println("第${i+1}轮排序后：${array.toJsonString()}")
            }

            return array
        }

        /**
         * 插入排序
         * 插入排序（Insertion Sort）是一种简单直观的排序算法。
         * 它的工作方式类似于我们打牌时的整理牌序，它将待排序序列分为两部分：已排序部分和未排序部分。
         * 算法不断地从未排序部分取出元素，然后插入到已排序部分的正确位置，直到所有元素都排序完毕。
         */
        fun insertSort(array: IntArray) :IntArray {
            for (i in 1 until array.size) {
                // 待插入的数（未排序队列的第一个数）
                val key = array[i]
                // 初始下标（已排序队列的最后一个数的下标）
                var j = i - 1
                // 将大于key的元素后移
                while (j >= 0 && array[j] > key) {
                    array[j + 1] = array[j]
                    j--
                }
                // 找到key的正确位置，插入
                array[j + 1] = key

                println("第${i}轮排序后：${array.toJsonString()}")
            }
            return array
        }
    }
}


fun main() {
    val list = createList<Int>(10) { randomInt(100) }.toIntArray()
    println("排序前：${list.toJsonString()}")
    println("排序后：${SortUtils.insertSort(list).toJsonString()}")
}