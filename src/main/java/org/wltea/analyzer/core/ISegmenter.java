/*
 * IK 中文分词  版本 8.1.1
 * IK Analyzer release 8.1.1
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 *
 * 8.1.1版本 由 Magese (magese@live.cn) 更新
 * release 8.1.1 update by Magese(magese@live.cn)
 *
 */
package org.wltea.analyzer.core;


/**
 * 
 * 子分词器接口
 */
interface ISegmenter {
	
	/**
	 * 从分析器读取下一个可能分解的词元对象
	 * @param context 分词算法上下文
	 */
	void analyze(AnalyzeContext context);
	
	
	/**
	 * 重置子分析器状态
	 */
	void reset();

}
