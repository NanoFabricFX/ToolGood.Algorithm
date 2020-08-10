package toolgood.algorithm;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import toolgood.algorithm.Operand;
import toolgood.algorithm.litJson.JsonData;
import toolgood.algorithm.litJson.JsonMapper;
import toolgood.algorithm.math.mathLexer;
import toolgood.algorithm.math.mathParser2.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
public class AlgorithmEngine {
    public boolean UseExcelIndex = true;
    public String LastError;
    private ProgContext _context;
    private final Map<String, Operand> _dict = new HashMap<String, Operand>();
    public Func<String, List<Operand>, Operand> DiyFunction;
    protected Operand GetParameter(final String parameter) {
        if (_dict.containsKey(parameter)) {
            return _dict.get(parameter);
        }
        return Operand.Error("Parameter [" + parameter + "] is missing.");
    }
    protected Operand ExecuteDiyFunction(final String funcName, final List<Operand> operands) {
        if (DiyFunction != null) {
            return DiyFunction.Invoke(funcName, operands);
        }
        return Operand.Error("DiyFunction [" + funcName + "] is missing.");
    }
    public void ClearDiyFunctions() {
        DiyFunction = null;
    }
    public void ClearParameters() {
        _dict.clear();
    }
    public void AddParameter(final String key, final Operand obj) {
        _dict.put(key, obj);
    }
    public void AddParameter(final String key, final boolean obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final short obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final int obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final long obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final float obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final double obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final BigDecimal obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final String obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final Date obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final DateTime obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final TimeSpan obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final List<Operand> obj) {
        _dict.put(key, Operand.Create(obj));
    }
    public void AddParameter(final String key, final List<String> obj) {
        List<Operand> array = new ArrayList<Operand>();
        for (String item : obj) {
            array.Add(Create(item));
        }
        _dict.put(key, Operand.Create(array));
    }
    public void AddParameter(final String key, final List<Double> obj) {
        List<Operand> array = new ArrayList<Operand>();
        for (Double item : obj) {
            array.Add(Create(item));
        }
        _dict.put(key, Operand.Create(array));
    }
    public void AddParameter(final String key, final List<Integer> obj) {
        List<Operand> array = new ArrayList<Operand>();
        for (Integer item : obj) {
            array.Add(Create(item));
        }
        _dict.put(key, Operand.Create(array));
    }
    public void AddParameter(final String key, final List<Boolean> obj) {
        List<Operand> array = new ArrayList<Operand>();
        for (Boolean item : obj) {
            array.Add(Create(item));
        }
        _dict.put(key, Operand.Create(array));
    }
    public void AddParameterFromJson(final String json)
        {
            if (json.startsWith("{") && json.endsWith("}")) {
                final JsonData jo = (JsonData)JsonMapper.ToObject(json);
                if (jo.IsObject()) {
                    for (String item : jo.inst_object.keySet()) {
                        final JsonData v = jo.inst_object.get(item);
                        if (v.IsString())
                            _dict.put(item, Operand.Create(v.StringValue()));
                        else if (v.IsBoolean())
                            _dict.put(item, Operand.Create(v.BooleanValue()));
                        else if (v.IsDouble())
                            _dict.put(item, Operand.Create(v.NumberValue()));
                        else if (v.IsObject())
                            _dict.put(item, Operand.Create(v));
                        else if (v.IsArray())
                            _dict.put(item, Operand.Create(v));
                        else if (v.IsNull())
                            _dict.put(item, Operand.CreateNull());
                    }
                    return;
                }
            }
            throw new Exception("Parameter is not json String.");
        }
    class AntlrErrorListener extends AntlrErrorListener<Token> {
        public boolean IsError;
        public String ErrorMsg;
        public void SyntaxError(final TextWriter output, final IRecognizer recognizer, final IToken offendingSymbol,
                final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
            IsError = true;
            ErrorMsg = msg;
        }
    }
    public boolean Parse(final String exp) throws RecognitionException  {
        if (String.isNullOrWhiteSpace(exp)) {
            LastError = "Parameter exp invalid !";
            return false;
        }
        // try {
        final AntlrInputStream stream = new CaseChangingCharStream(new AntlrInputStream(exp));
        final mathLexer lexer = new mathLexer(stream);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final mathParser parser = new mathParser(tokens);
        final AntlrErrorListener antlrErrorListener = new AntlrErrorListener();
        parser.RemoveErrorListeners();
        parser.AddErrorListener(antlrErrorListener);
        final ProgContext context = parser.prog();
        final int end = context.Stop.StopIndex;
        if (end + 1 < exp.length()) {
            _context = null;
            LastError = "Parameter exp invalid !";
            return false;
        }
        if (antlrErrorListener.IsError) {
            _context = null;
            LastError = antlrErrorListener.ErrorMsg;
            return false;
        }
        _context = context;
        return true;
        // } catch (Exception ex) {
        // LastError = ex.Message;
        // return false;
        // }
    }
    public Operand Evaluate() {
        if (_context == null) {
            LastError = "Please use Parse to compile formula !";
            throw new Exception("Please use Parse to compile formula !");
        }
        final MathVisitor visitor = new MathVisitor();
        visitor.GetParameter += GetParameter;
        visitor.excelIndex = UseExcelIndex ? 1 : 0;
        visitor.DiyFunction += ExecuteDiyFunction;
        return visitor.Visit(_context);
    }
    public int TryEvaluate(final String exp, final int defvalue) {
        try {
            if (Parse(exp)) {
                Operand obj = Evaluate();
                obj = obj.ToNumber("It can't be converted to number!");
                if (obj.IsError()) {
                    LastError = obj.ErrorMsg;
                    return defvalue;
                }
                return obj.IntValue;
            }
        } catch (final Exception ex) {
            LastError = ex.Message;
        }
        return defvalue;
    }
    public double TryEvaluate(final String exp, final double defvalue) {
        try {
            if (Parse(exp)) {
                Operand obj = Evaluate();
                obj = obj.ToNumber("It can't be converted to number!");
                if (obj.IsError()) {
                    LastError = obj.ErrorMsg;
                    return defvalue;
                }
                return obj.NumberValue;
            }
        } catch (final Exception ex) {
            LastError = ex.Message;
        }
        return defvalue;
    }
    public String TryEvaluate(final String exp, final String defvalue) {
        try {
            if (Parse(exp)) {
                Operand obj = Evaluate();
                if (obj.IsNull) {
                    return null;
                }
                obj = obj.ToText("It can't be converted to String!");
                if (obj.IsError()) {
                    LastError = obj.ErrorMsg;
                    return defvalue;
                }
                return obj.TextValue;
            }
        } catch (final Exception ex) {
            LastError = ex.Message;
        }
        return defvalue;
    }
    public boolean TryEvaluate(final String exp, final boolean defvalue) {
        try {
            if (Parse(exp)) {
                Operand obj = Evaluate();
                obj = obj.ToBoolean("It can't be converted to bool!");
                if (obj.IsError()) {
                    LastError = obj.ErrorMsg;
                    return defvalue;
                }
                return obj.BooleanValue;
            }
        } catch (final Exception ex) {
            LastError = ex.Message;
        }
        return defvalue;
    }
    public DateTime TryEvaluate(final String exp, final DateTime defvalue) {
        try {
            if (Parse(exp)) {
                Operand obj = Evaluate();
                obj = obj.ToDate("It can't be converted to date!");
                if (obj.IsError()) {
                    LastError = obj.ErrorMsg;
                    return defvalue;
                }
                return (DateTime) obj.DateValue;
            }
        } catch (final Exception ex) {
            LastError = ex.Message;
        }
        return defvalue;
    }
    public TimeSpan TryEvaluate(final String exp, final TimeSpan defvalue) {
        try {
            if (Parse(exp)) {
                Operand obj = Evaluate();
                obj = obj.ToDate("It can't be converted to date!");
                if (obj.IsError()) {
                    LastError = obj.ErrorMsg;
                    return defvalue;
                }
                return (TimeSpan) obj.DateValue;
            }
        } catch (final Exception ex) {
            LastError = ex.Message;
        }
        return defvalue;
    }
}