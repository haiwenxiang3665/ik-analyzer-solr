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
package org.wltea.analyzer.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * IK简易查询表达式解析
 * 结合SWMCQuery算法
 *
 * @author linliangyi
 */
public class IKQueryExpressionParser {


    private List<Element> elements = new ArrayList<>();

    private Stack<Query> querys = new Stack<>();

    private Stack<Element> operates = new Stack<>();

    /**
     * 解析查询表达式，生成Lucene Query对象
     *
     * @return Lucene query
     */
    private Query parseExp(String expression) {
        Query lucenceQuery = null;
        if (expression != null && !"".equals(expression.trim())) {
            try {
                //文法解析
                this.splitElements(expression);
                //语法解析
                this.parseSyntax();
                if (this.querys.size() == 1) {
                    lucenceQuery = this.querys.pop();
                } else {
                    throw new IllegalStateException("表达式异常： 缺少逻辑操作符 或 括号缺失");
                }
            } finally {
                elements.clear();
                querys.clear();
                operates.clear();
            }
        }
        return lucenceQuery;
    }

    /**
     * 表达式文法解析
     */
    private void splitElements(String expression) {

        if (expression == null) {
            return;
        }
        Element curretElement = null;

        char[] expChars = expression.toCharArray();
        for (char expChar : expChars) {
            switch (expChar) {
                case '&':
                    if (curretElement == null) {
                        curretElement = new Element();
                        curretElement.type = '&';
                        curretElement.append(expChar);
                    } else if (curretElement.type == '&') {
                        curretElement.append(expChar);
                        this.elements.add(curretElement);
                        curretElement = null;
                    } else if (curretElement.type == '\'') {
                        curretElement.append(expChar);
                    } else {
                        this.elements.add(curretElement);
                        curretElement = new Element();
                        curretElement.type = '&';
                        curretElement.append(expChar);
                    }
                    break;

                case '|':
                    if (curretElement == null) {
                        curretElement = new Element();
                        curretElement.type = '|';
                        curretElement.append(expChar);
                    } else if (curretElement.type == '|') {
                        curretElement.append(expChar);
                        this.elements.add(curretElement);
                        curretElement = null;
                    } else if (curretElement.type == '\'') {
                        curretElement.append(expChar);
                    } else {
                        this.elements.add(curretElement);
                        curretElement = new Element();
                        curretElement.type = '|';
                        curretElement.append(expChar);
                    }
                    break;

                case '-':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                            continue;
                        } else {
                            this.elements.add(curretElement);
                        }
                    }
                    curretElement = new Element();
                    curretElement.type = '-';
                    curretElement.append(expChar);
                    this.elements.add(curretElement);
                    curretElement = null;
                    break;

                case '(':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                            continue;
                        } else {
                            this.elements.add(curretElement);
                        }
                    }
                    curretElement = new Element();
                    curretElement.type = '(';
                    curretElement.append(expChar);
                    this.elements.add(curretElement);
                    curretElement = null;
                    break;

                case ')':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                            continue;
                        } else {
                            this.elements.add(curretElement);
                        }
                    }
                    curretElement = new Element();
                    curretElement.type = ')';
                    curretElement.append(expChar);
                    this.elements.add(curretElement);
                    curretElement = null;
                    break;

                case ':':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                            continue;
                        } else {
                            this.elements.add(curretElement);
                        }
                    }
                    curretElement = new Element();
                    curretElement.type = ':';
                    curretElement.append(expChar);
                    this.elements.add(curretElement);
                    curretElement = null;
                    break;

                case '=':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                            continue;
                        } else {
                            this.elements.add(curretElement);
                        }
                    }
                    curretElement = new Element();
                    curretElement.type = '=';
                    curretElement.append(expChar);
                    this.elements.add(curretElement);
                    curretElement = null;
                    break;

                case ' ':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                        } else {
                            this.elements.add(curretElement);
                            curretElement = null;
                        }
                    }

                    break;

                case '\'':
                    if (curretElement == null) {
                        curretElement = new Element();
                        curretElement.type = '\'';

                    } else if (curretElement.type == '\'') {
                        this.elements.add(curretElement);
                        curretElement = null;

                    } else {
                        this.elements.add(curretElement);
                        curretElement = new Element();
                        curretElement.type = '\'';

                    }
                    break;

                case '[':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                            continue;
                        } else {
                            this.elements.add(curretElement);
                        }
                    }
                    curretElement = new Element();
                    curretElement.type = '[';
                    curretElement.append(expChar);
                    this.elements.add(curretElement);
                    curretElement = null;
                    break;

                case ']':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                            continue;
                        } else {
                            this.elements.add(curretElement);
                        }
                    }
                    curretElement = new Element();
                    curretElement.type = ']';
                    curretElement.append(expChar);
                    this.elements.add(curretElement);
                    curretElement = null;

                    break;

                case '{':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                            continue;
                        } else {
                            this.elements.add(curretElement);
                        }
                    }
                    curretElement = new Element();
                    curretElement.type = '{';
                    curretElement.append(expChar);
                    this.elements.add(curretElement);
                    curretElement = null;
                    break;

                case '}':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                            continue;
                        } else {
                            this.elements.add(curretElement);
                        }
                    }
                    curretElement = new Element();
                    curretElement.type = '}';
                    curretElement.append(expChar);
                    this.elements.add(curretElement);
                    curretElement = null;

                    break;
                case ',':
                    if (curretElement != null) {
                        if (curretElement.type == '\'') {
                            curretElement.append(expChar);
                            continue;
                        } else {
                            this.elements.add(curretElement);
                        }
                    }
                    curretElement = new Element();
                    curretElement.type = ',';
                    curretElement.append(expChar);
                    this.elements.add(curretElement);
                    curretElement = null;

                    break;

                default:
                    if (curretElement == null) {
                        curretElement = new Element();
                        curretElement.type = 'F';
                        curretElement.append(expChar);

                    } else if (curretElement.type == 'F') {
                        curretElement.append(expChar);

                    } else if (curretElement.type == '\'') {
                        curretElement.append(expChar);

                    } else {
                        this.elements.add(curretElement);
                        curretElement = new Element();
                        curretElement.type = 'F';
                        curretElement.append(expChar);
                    }
            }
        }

        if (curretElement != null) {
            this.elements.add(curretElement);
        }
    }

    /**
     * 语法解析
     */
    private void parseSyntax() {
        for (int i = 0; i < this.elements.size(); i++) {
            Element e = this.elements.get(i);
            if ('F' == e.type) {
                Element e2 = this.elements.get(i + 1);
                if ('=' != e2.type && ':' != e2.type) {
                    throw new IllegalStateException("表达式异常： = 或 ： 号丢失");
                }
                Element e3 = this.elements.get(i + 2);
                //处理 = 和 ： 运算
                if ('\'' == e3.type) {
                    i += 2;
                    if ('=' == e2.type) {
                        TermQuery tQuery = new TermQuery(new Term(e.toString(), e3.toString()));
                        this.querys.push(tQuery);
                    } else {
                        String keyword = e3.toString();
                        //SWMCQuery Here
                        Query _SWMCQuery = SWMCQueryBuilder.create(e.toString(), keyword);
                        this.querys.push(_SWMCQuery);
                    }

                } else if ('[' == e3.type || '{' == e3.type) {
                    i += 2;
                    //处理 [] 和 {}
                    LinkedList<Element> eQueue = new LinkedList<>();
                    eQueue.add(e3);
                    for (i++; i < this.elements.size(); i++) {
                        Element eN = this.elements.get(i);
                        eQueue.add(eN);
                        if (']' == eN.type || '}' == eN.type) {
                            break;
                        }
                    }
                    //翻译RangeQuery
                    Query rangeQuery = this.toTermRangeQuery(e, eQueue);
                    this.querys.push(rangeQuery);
                } else {
                    throw new IllegalStateException("表达式异常：匹配值丢失");
                }

            } else if ('(' == e.type) {
                this.operates.push(e);

            } else if (')' == e.type) {
                boolean doPop = true;
                while (doPop && !this.operates.empty()) {
                    Element op = this.operates.pop();
                    if ('(' == op.type) {
                        doPop = false;
                    } else {
                        Query q = toBooleanQuery(op);
                        this.querys.push(q);
                    }

                }
            } else {

                if (this.operates.isEmpty()) {
                    this.operates.push(e);
                } else {
                    boolean doPeek = true;
                    while (doPeek && !this.operates.isEmpty()) {
                        Element eleOnTop = this.operates.peek();
                        if ('(' == eleOnTop.type) {
                            doPeek = false;
                            this.operates.push(e);
                        } else if (compare(e, eleOnTop) == 1) {
                            this.operates.push(e);
                            doPeek = false;
                        } else if (compare(e, eleOnTop) == 0) {
                            Query q = toBooleanQuery(eleOnTop);
                            this.operates.pop();
                            this.querys.push(q);
                        } else {
                            Query q = toBooleanQuery(eleOnTop);
                            this.operates.pop();
                            this.querys.push(q);
                        }
                    }

                    if (doPeek && this.operates.empty()) {
                        this.operates.push(e);
                    }
                }
            }
        }

        while (!this.operates.isEmpty()) {
            Element eleOnTop = this.operates.pop();
            Query q = toBooleanQuery(eleOnTop);
            this.querys.push(q);
        }
    }

    /**
     * 根据逻辑操作符，生成BooleanQuery
     */
    private Query toBooleanQuery(Element op) {
        if (this.querys.size() == 0) {
            return null;
        }

        BooleanQuery.Builder resultQuery = new BooleanQuery.Builder();

        if (this.querys.size() == 1) {
            return this.querys.get(0);
        }

        Query q2 = this.querys.pop();
        Query q1 = this.querys.pop();
        if ('&' == op.type) {
            if (q1 != null) {
                if (q1 instanceof BooleanQuery) {
                    List<BooleanClause> clauses = ((BooleanQuery) q1).clauses();
                    if (clauses.size() > 0
                            && clauses.get(0).getOccur() == Occur.MUST) {
                        for (BooleanClause c : clauses) {
                            resultQuery.add(c);
                        }
                    } else {
                        resultQuery.add(q1, Occur.MUST);
                    }

                } else {
                    //q1 instanceof TermQuery
                    //q1 instanceof TermRangeQuery
                    //q1 instanceof PhraseQuery
                    //others
                    resultQuery.add(q1, Occur.MUST);
                }
            }

            if (q2 != null) {
                if (q2 instanceof BooleanQuery) {
                    List<BooleanClause> clauses = ((BooleanQuery) q2).clauses();
                    if (clauses.size() > 0
                            && clauses.get(0).getOccur() == Occur.MUST) {
                        for (BooleanClause c : clauses) {
                            resultQuery.add(c);
                        }
                    } else {
                        resultQuery.add(q2, Occur.MUST);
                    }

                } else {
                    //q1 instanceof TermQuery
                    //q1 instanceof TermRangeQuery
                    //q1 instanceof PhraseQuery
                    //others
                    resultQuery.add(q2, Occur.MUST);
                }
            }

        } else if ('|' == op.type) {
            if (q1 != null) {
                if (q1 instanceof BooleanQuery) {
                    List<BooleanClause> clauses = ((BooleanQuery) q1).clauses();
                    if (clauses.size() > 0
                            && clauses.get(0).getOccur() == Occur.SHOULD) {
                        for (BooleanClause c : clauses) {
                            resultQuery.add(c);
                        }
                    } else {
                        resultQuery.add(q1, Occur.SHOULD);
                    }

                } else {
                    //q1 instanceof TermQuery
                    //q1 instanceof TermRangeQuery
                    //q1 instanceof PhraseQuery
                    //others
                    resultQuery.add(q1, Occur.SHOULD);
                }
            }

            if (q2 != null) {
                if (q2 instanceof BooleanQuery) {
                    List<BooleanClause> clauses = ((BooleanQuery) q2).clauses();
                    if (clauses.size() > 0
                            && clauses.get(0).getOccur() == Occur.SHOULD) {
                        for (BooleanClause c : clauses) {
                            resultQuery.add(c);
                        }
                    } else {
                        resultQuery.add(q2, Occur.SHOULD);
                    }
                } else {
                    //q2 instanceof TermQuery
                    //q2 instanceof TermRangeQuery
                    //q2 instanceof PhraseQuery
                    //others
                    resultQuery.add(q2, Occur.SHOULD);

                }
            }

        } else if ('-' == op.type) {
            if (q1 == null || q2 == null) {
                throw new IllegalStateException("表达式异常：SubQuery 个数不匹配");
            }

            if (q1 instanceof BooleanQuery) {
                List<BooleanClause> clauses = ((BooleanQuery) q1).clauses();
                if (clauses.size() > 0) {
                    for (BooleanClause c : clauses) {
                        resultQuery.add(c);
                    }
                } else {
                    resultQuery.add(q1, Occur.MUST);
                }

            } else {
                //q1 instanceof TermQuery
                //q1 instanceof TermRangeQuery
                //q1 instanceof PhraseQuery
                //others
                resultQuery.add(q1, Occur.MUST);
            }

            resultQuery.add(q2, Occur.MUST_NOT);
        }
        return resultQuery.build();
    }

    /**
     * 组装TermRangeQuery
     */
    private TermRangeQuery toTermRangeQuery(Element fieldNameEle, LinkedList<Element> elements) {

        boolean includeFirst;
        boolean includeLast;
        String firstValue;
        String lastValue = null;
        //检查第一个元素是否是[或者{
        Element first = elements.getFirst();
        if ('[' == first.type) {
            includeFirst = true;
        } else if ('{' == first.type) {
            includeFirst = false;
        } else {
            throw new IllegalStateException("表达式异常");
        }
        //检查最后一个元素是否是]或者}
        Element last = elements.getLast();
        if (']' == last.type) {
            includeLast = true;
        } else if ('}' == last.type) {
            includeLast = false;
        } else {
            throw new IllegalStateException("表达式异常, RangeQuery缺少结束括号");
        }
        if (elements.size() < 4 || elements.size() > 5) {
            throw new IllegalStateException("表达式异常, RangeQuery 错误");
        }
        //读出中间部分
        Element e2 = elements.get(1);
        if ('\'' == e2.type) {
            firstValue = e2.toString();
            //
            Element e3 = elements.get(2);
            if (',' != e3.type) {
                throw new IllegalStateException("表达式异常, RangeQuery缺少逗号分隔");
            }
            //
            Element e4 = elements.get(3);
            if ('\'' == e4.type) {
                lastValue = e4.toString();
            } else if (e4 != last) {
                throw new IllegalStateException("表达式异常，RangeQuery格式错误");
            }
        } else if (',' == e2.type) {
            firstValue = null;
            //
            Element e3 = elements.get(2);
            if ('\'' == e3.type) {
                lastValue = e3.toString();
            } else {
                throw new IllegalStateException("表达式异常，RangeQuery格式错误");
            }

        } else {
            throw new IllegalStateException("表达式异常, RangeQuery格式错误");
        }

        assert firstValue != null;
        assert lastValue != null;
        return new TermRangeQuery(fieldNameEle.toString(), new BytesRef(firstValue), new BytesRef(lastValue), includeFirst, includeLast);
    }

    /**
     * 比较操作符优先级
     */
    private int compare(Element e1, Element e2) {
        if ('&' == e1.type) {
            if ('&' == e2.type) {
                return 0;
            } else {
                return 1;
            }
        } else if ('|' == e1.type) {
            if ('&' == e2.type) {
                return -1;
            } else if ('|' == e2.type) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if ('-' == e2.type) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    /**
     * 表达式元素（操作符、FieldName、FieldValue）
     *
     * @author linliangyi
     * May 20, 2010
     */
    private class Element {
        char type = 0;
        StringBuffer eleTextBuff;

        Element() {
            eleTextBuff = new StringBuffer();
        }

        void append(char c) {
            this.eleTextBuff.append(c);
        }

        public String toString() {
            return this.eleTextBuff.toString();
        }
    }

    public static void main(String[] args) {
        IKQueryExpressionParser parser = new IKQueryExpressionParser();
        //String ikQueryExp = "newsTitle:'的两款《魔兽世界》插件Bigfoot和月光宝盒'";
        String ikQueryExp = "(id='ABcdRf' && date:{'20010101','20110101'} && keyword:'魔兽中国') || (content:'KSHT-KSH-A001-18'  || ulr='www.ik.com') - name:'林良益'";
        Query result = parser.parseExp(ikQueryExp);
        System.out.println(result);

    }

}
