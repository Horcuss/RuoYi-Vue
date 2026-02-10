package com.ruoyi.monitor.utils;

import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

/**
 * 表达式计算工具类
 * 支持简单的算术运算：+、-、*、/、()
 * 
 * @author ruoyi
 * @date 2025-01-15
 */
public class ExpressionCalculator 
{
    private static final Logger log = LoggerFactory.getLogger(ExpressionCalculator.class);

    /**
     * 计算算术表达式
     * 
     * @param expression 表达式字符串，如："100 * 2 + 50"
     * @return 计算结果
     */
    public static Double calculate(String expression) 
    {
        if (StringUtils.isEmpty(expression)) 
        {
            return null;
        }

        try 
        {
            // 去除空格
            expression = expression.replaceAll("\\s+", "");
            
            // 使用双栈法计算表达式
            return evaluateExpression(expression);
        } 
        catch (Exception e) 
        {
            log.error("表达式计算失败: {}", expression, e);
            return null;
        }
    }

    /**
     * 使用双栈法计算表达式
     * 
     * @param expression 表达式
     * @return 计算结果
     */
    private static Double evaluateExpression(String expression) 
    {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        
        int i = 0;
        while (i < expression.length()) 
        {
            char c = expression.charAt(i);
            
            // 如果是数字或小数点，或者是负号（一元运算符）
            if (Character.isDigit(c) || c == '.') 
            {
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() && 
                       (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) 
                {
                    sb.append(expression.charAt(i));
                    i++;
                }
                numbers.push(Double.parseDouble(sb.toString()));
                continue;
            }
            // 处理负号（一元运算符）- 在表达式开头或左括号/运算符之后
            else if (c == '-' && (i == 0 || expression.charAt(i - 1) == '(' || isOperator(expression.charAt(i - 1))))
            {
                StringBuilder sb = new StringBuilder();
                sb.append('-');
                i++;
                // 读取负数
                while (i < expression.length() && 
                       (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) 
                {
                    sb.append(expression.charAt(i));
                    i++;
                }
                numbers.push(Double.parseDouble(sb.toString()));
                continue;
            }
            // 如果是左括号
            else if (c == '(') 
            {
                operators.push(c);
            }
            // 如果是右括号
            else if (c == ')') 
            {
                while (!operators.isEmpty() && operators.peek() != '(') 
                {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.pop(); // 弹出左括号
            }
            // 如果是运算符
            else if (isOperator(c)) 
            {
                while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) 
                {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(c);
            }
            
            i++;
        }
        
        // 处理剩余的运算符
        while (!operators.isEmpty()) 
        {
            numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
        }
        
        return numbers.pop();
    }

    /**
     * 判断是否为运算符
     */
    private static boolean isOperator(char c) 
    {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    /**
     * 判断运算符优先级
     */
    private static boolean hasPrecedence(char op1, char op2) 
    {
        if (op2 == '(' || op2 == ')') 
        {
            return false;
        }
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) 
        {
            return false;
        }
        return true;
    }

    /**
     * 执行运算
     */
    private static Double applyOperation(char operator, double b, double a) 
    {
        switch (operator) 
        {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) 
                {
                    throw new ArithmeticException("除数不能为0");
                }
                return a / b;
            default:
                return 0.0;
        }
    }

    /**
     * 替换表达式中的变量并计算
     * 
     * @param expression 表达式，如："value * 100"
     * @param value 变量值
     * @return 计算结果
     */
    public static Double calculateWithVariable(String expression, Object value) 
    {
        if (StringUtils.isEmpty(expression) || value == null) 
        {
            return null;
        }

        try 
        {
            // 将value转换为数字
            String valueStr = value.toString();
            
            // 替换表达式中的变量名为实际值
            // 支持常见的变量名：value, val, x, data等
            String[] variables = {"value", "val", "x", "data", "result"};
            for (String var : variables) 
            {
                expression = expression.replaceAll("\\b" + var + "\\b", valueStr);
            }
            
            return calculate(expression);
        } 
        catch (Exception e) 
        {
            log.error("表达式计算失败: expression={}, value={}", expression, value, e);
            return null;
        }
    }
}

