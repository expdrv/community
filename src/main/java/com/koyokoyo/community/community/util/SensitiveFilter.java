package com.koyokoyo.community.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger= LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT="***";

    private TrieNode root=new TrieNode();

    @PostConstruct
    public void init()
    {

        try(InputStream is=this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader=new BufferedReader(new InputStreamReader(is));
        )
        {
            String keyword;
            while((keyword=reader.readLine())!=null)
            {
                addKeyword(keyword);
            }
        }
        catch (IOException e)
        {
            logger.error("获取敏感词文件失败"+e.getMessage());
        }
    }

    /**
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text)
    {
        if(StringUtils.isBlank(text))
            return null;
        //3 key pointers

        TrieNode tempNode=root;

        int begin=0;
        int end=0;

        //结果
        StringBuilder sb=new StringBuilder();

        while(begin<text.length())
        {
            char c=text.charAt(end);
            //跳过中间的符号
            if(isSymbol(c))
            {
             //若指针1处于根节点，则将此符号写入，并将指针二前移
                if(tempNode==root)
                {
                    sb.append(c);
                    begin++;
                }
                end++;
                continue;
            }
            //检查下个节点
            tempNode =tempNode.getSubNode(c);
            if(tempNode==null)
            {
                sb.append(text.charAt(end));
                end=++begin;
                tempNode=root;
            }
            else if(tempNode.isKeywordEnd())
            {
             //确认为敏感词
                sb.append(REPLACEMENT);
                begin=++end;
            }
            //检查下一字符
            else
            {
                if(end<text.length()-1)
                    end++;
            }
        }
        sb.append(text.substring(begin));
        return  sb.toString();
    }

    private boolean isSymbol(Character c)
    {
        //0x2e80到0x9fff是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }

    private void addKeyword(String keyword)
    {
        TrieNode tempNode=root;
        for(int i=0;i<keyword.length();i++)
        {
            char c=keyword.charAt(i);
            TrieNode subNode=tempNode.getSubNode(c);
            if(subNode==null)
            {
                subNode=new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            tempNode=subNode;
        }
        tempNode.setKeywordEnd(true);
    }
    //Trie
    private class TrieNode
    {
        private boolean isKeywordEnd=false;

        private Map<Character,TrieNode> subNodes=new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c,TrieNode node)
        {
            subNodes.put(c,node);
        }

        public TrieNode getSubNode(Character c)
        {
            return subNodes.get(c);
        }


    }
}
