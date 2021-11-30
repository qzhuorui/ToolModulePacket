package com.choryan.quan.wxclean.adapter

import java.io.File

/**
 * @author: ChoRyan Quan
 * @date: 2021/11/30
 * @description:
 */
data class FileBean(val file: File, val type: Int, var select: Boolean = false)
