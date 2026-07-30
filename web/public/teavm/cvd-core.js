"use strict";
let $rt_seed = 2463534242,
$rt_nextId = () => {
    let x = $rt_seed;
    x ^= x << 13;
    x ^= x >>> 17;
    x ^= x << 5;
    $rt_seed = x;
    return x;
},
$rt_wrapFunction0 = f => function() {
    return f(this);
},
$rt_wrapFunction1 = f => function(p1) {
    return f(this, p1);
},
$rt_wrapFunction2 = f => function(p1, p2) {
    return f(this, p1, p2);
},
$rt_mainStarter = f => (args, callback) => {
    if (!args) {
        args = [];
    }
    let javaArgs = $rt_createArray($rt_objcls(), args.length);
    for (let i = 0;i < args.length;++i) {
        javaArgs.data[i] = $rt_str(args[i]);
    }
    $rt_startThread(() => {
        f.call(null, javaArgs);
    }, callback);
},
$rt_eraseClinit = target => target.$clinit = () => {
},
$dbg_class = obj => {
    let cls = obj.constructor;
    let arrayDegree = 0;
    while (cls[$rt_meta] && cls[$rt_meta].item) {
        ++arrayDegree;
        cls = cls[$rt_meta].item;
    }
    let clsName = "";
    if (cls[$rt_meta].primitiveKind !== 0) {
        clsName = cls[$rt_meta].name;
    } else {
        clsName = cls[$rt_meta] ? cls[$rt_meta].name || "a/" + cls.name : "@" + cls.name;
    }
    while (arrayDegree-- > 0) {
        clsName += "[]";
    }
    return clsName;
},
$rt_classWithoutFields = superclass => {
    if (superclass === 0) {
        return function() {
        };
    }
    if (superclass === void 0) {
        superclass = $rt_objcls();
    }
    return function() {
        superclass.call(this);
    };
},
$rt_meta = Symbol("teavm_meta"),
$rt_cls = cls => {
    if (cls[$rt_meta].classObject === null) {
        cls[$rt_meta].classObject = jl_Class_createClass(cls);
    }
    return cls[$rt_meta].classObject;
},
$rt_objcls = () => jl_Object,
$rt_newClassMetadata = source => {
    return Object.assign({ name : null, binaryName : null, parent : null, superinterfaces : [], modifiers : 0, primitiveKind : 0, itemType : null, arrayType : null, enclosingClass : null, declaringClass : null, simpleName : null, clinit : () => {
    }, constructor : null, enumConstants : () => null, resolvedEnumConstants : null, reflection : null, classObject : null, assignableCache : null, valueToObject : o => o, objectToValue : o => o }, source || {  });
},
$rt_createPrimitiveCls = (name, binaryName, kind, config) => {
    let cls = () => {
    };
    let meta = $rt_newClassMetadata({ name : name, binaryName : binaryName, modifiers : 1 | 1 << 4, primitiveKind : kind });
    cls[$rt_meta] = meta;
    if (typeof config === 'function') {
        config(meta);
    }
    return cls;
},
$rt_booleancls = $rt_createPrimitiveCls("boolean", "Z", 1, meta => {
}),
$rt_charcls = $rt_createPrimitiveCls("char", "C", 4, meta => {
}),
$rt_intcls = $rt_createPrimitiveCls("int", "I", 5, meta => {
}),
$rt_doublecls = $rt_createPrimitiveCls("double", "D", 8, meta => {
    {
        meta.valueToObject = o => jl_Double_valueOf(o);
    }
    {
        meta.objectToValue = o => jl_Double_doubleValue(o);
    }
}),
$rt_voidcls = $rt_createPrimitiveCls("void", "V", 9),
$rt_compare = (a, b) => a === b ? 0 : a < b ?  -1 : 1,
$rt_imul = Math.imul || function(a, b) {
    let ah = a >>> 16 & 0xFFFF;
    let al = a & 0xFFFF;
    let bh = b >>> 16 & 0xFFFF;
    let bl = b & 0xFFFF;
    return al * bl + (ah * bl + al * bh << 16 >>> 0) | 0;
},
$rt_udiv = (a, b) => (a >>> 0) / (b >>> 0) >>> 0,
$rt_umod = (a, b) => (a >>> 0) % (b >>> 0) >>> 0,
$rt_ucmp = (a, b) => {
    a >>>= 0;
    b >>>= 0;
    return a < b ?  -1 : a > b ? 1 : 0;
},
Long_fromNumber = val => BigInt.asIntN(64, BigInt(val >= 0 ? Math.floor(val) : Math.ceil(val))),
Long_lo = val => Number(BigInt.asIntN(32, val)) | 0,
$rt_createArray = (cls, sz) => {
    let data = new Array(sz);
    data.fill(null);
    return new ($rt_arraycls(cls))(data);
},
$rt_wrapArray = (cls, data) => new ($rt_arraycls(cls))(data),
$rt_createCharArray = sz => new $rt_charArrayCls(new Uint16Array(sz)),
$rt_createIntArray = sz => new $rt_intArrayCls(new Int32Array(sz)),
$rt_createIntArrayFromData = data => {
    let buffer = new Int32Array(data.length);
    buffer.set(data);
    return new $rt_intArrayCls(buffer);
},
$rt_createBooleanArray = sz => new $rt_booleanArrayCls(new Int8Array(sz)),
$rt_createDoubleArray = sz => new $rt_doubleArrayCls(new Float64Array(sz)),
$rt_createDoubleArrayFromData = data => {
    let buffer = new Float64Array(data.length);
    buffer.set(data);
    return new $rt_doubleArrayCls(buffer);
},
$rt_arraycls = cls => {
    let result = cls[$rt_meta].arrayType;
    if (result === null) {
        function JavaArray(data) {
            ($rt_objcls()).call(this);
            this.data = data;
        }
        JavaArray.prototype = Object.create(($rt_objcls()).prototype);
        JavaArray.prototype.type = cls;
        JavaArray.prototype.constructor = JavaArray;
        JavaArray.prototype.toString = function() {
            let str = "[";
            for (let i = 0;i < this.data.length;++i) {
                if (i > 0) {
                    str += ", ";
                }
                str += this.data[i].toString();
            }
            str += "]";
            return str;
        };
        JavaArray.prototype.$clone0 = function() {
            let dataCopy;
            if ('slice' in this.data) {
                dataCopy = this.data.slice();
            } else {
                dataCopy = new this.data.constructor(this.data.length);
                for (let i = 0;i < dataCopy.length;++i) {
                    dataCopy[i] = this.data[i];
                }
            }
            return new ($rt_arraycls(this.type))(dataCopy);
        };
        let name = "[" + cls[$rt_meta].binaryName;
        JavaArray[$rt_meta] = $rt_newClassMetadata({ name : name, binaryName : name, parent : $rt_objcls(), itemType : cls });
        result = JavaArray;
        cls[$rt_meta].arrayType = JavaArray;
    }
    return result;
};
function $rt_arrayLength(array) {
    return array.data.length;
}
let $rt_stringPool_instance,
$rt_stringPool = strings => {
    $rt_stringClassInit();
    $rt_stringPool_instance = new Array(strings.length);
    for (let i = 0;i < strings.length;++i) {
        $rt_stringPool_instance[i] = $rt_intern($rt_str(strings[i]));
    }
},
$rt_s = index => $rt_stringPool_instance[index],
$rt_charArrayToString = (array, offset, count) => {
    let result = "";
    let limit = offset + count;
    for (let i = offset;i < limit;i = i + 1024 | 0) {
        let next = Math.min(limit, i + 1024 | 0);
        result += String.fromCharCode.apply(null, array.subarray(i, next));
    }
    return result;
},
$rt_str = str => str === null ? null : jl_String__init_(str),
$rt_ustr = str => str === null ? null : str.$nativeString,
$rt_stringClassInit = () => jl_String_$callClinit(),
$rt_intern;
{
    $rt_intern = str => str;
}
let $rt_enumConstants = cls => {
    let meta = cls[$rt_meta];
    if (meta.resolvedEnumConstants === null) {
        let result = meta.enumConstants();
        meta.resolvedEnumConstants = result !== null ? result : [];
    }
    return meta.resolvedEnumConstants;
},
$rt_isInstance = (obj, cls) => obj instanceof $rt_objcls() && !!obj.constructor[$rt_meta] && $rt_isAssignable(obj.constructor, cls),
$rt_isAssignable = (from, to) => {
    if (from === to) {
        return true;
    }
    let map = from[$rt_meta].assignableCache;
    if (map === null) {
        map = new Map();
        from[$rt_meta].assignableCache = map;
    }
    let cachedResult = map.get(to);
    if (typeof cachedResult !== 'undefined') {
        return cachedResult;
    }
    if (to[$rt_meta].itemType !== null) {
        let result = from[$rt_meta].itemType !== null && $rt_isAssignable(from[$rt_meta].itemType, to[$rt_meta].itemType);
        map.set(to, result);
        return result;
    }
    let parent = from[$rt_meta].parent;
    if (parent !== null && parent !== from) {
        if ($rt_isAssignable(parent, to)) {
            map.set(to, true);
            return true;
        }
    }
    let superinterfaces = from[$rt_meta].superinterfaces;
    for (let i = 0;i < superinterfaces.length;i = i + 1 | 0) {
        if ($rt_isAssignable(superinterfaces[i], to)) {
            map.set(to, true);
            return true;
        }
    }
    map.set(to, false);
    return false;
},
$rt_throw = ex => {
    throw $rt_exception(ex);
},
$rt_javaExceptionProp = Symbol("javaException"),
$rt_exception = ex => {
    if (!ex.$jsException) {
        $rt_fillNativeException(ex);
    }
    return ex.$jsException;
},
$rt_fillNativeException = ex => {
    let javaCause = $rt_throwableCause(ex);
    let jsCause = javaCause !== null ? javaCause.$jsException : void 0;
    let cause = typeof jsCause === "object" ? { cause : jsCause } : void 0;
    let err = new JavaError("Java exception thrown", cause);
    if (typeof Error.captureStackTrace === "function") {
        Error.captureStackTrace(err);
    }
    err[$rt_javaExceptionProp] = ex;
    ex.$jsException = err;
    $rt_fillStack(err, ex);
},
$rt_fillStack = (err, ex) => {
    if (typeof $rt_decodeStack === "function" && err.stack) {
        let stack = $rt_decodeStack(err.stack);
        let javaStack = $rt_createArray($rt_stecls(), stack.length);
        let elem;
        let noStack = false;
        for (let i = 0;i < stack.length;++i) {
            let element = stack[i];
            elem = $rt_createStackElement($rt_str(element.className), $rt_str(element.methodName), $rt_str(element.fileName), element.lineNumber);
            if (elem == null) {
                noStack = true;
                break;
            }
            javaStack.data[i] = elem;
        }
        if (!noStack) {
            $rt_setStack(ex, javaStack);
        }
    }
},
JavaError;
if (typeof Reflect === 'object') {
    let defaultMessage = Symbol("defaultMessage");
    JavaError = function JavaError(message, cause) {
        let self = Reflect.construct(Error, [void 0, cause], JavaError);
        Object.setPrototypeOf(self, JavaError.prototype);
        self[defaultMessage] = message;
        return self;
    }
    ;
    JavaError.prototype = Object.create(Error.prototype, { constructor : { configurable : true, writable : true, value : JavaError }, message : { get() {
        try {
            let javaException = this[$rt_javaExceptionProp];
            if (typeof javaException === 'object') {
                let javaMessage = $rt_throwableMessage(javaException);
                if (typeof javaMessage === "object") {
                    return javaMessage !== null ? javaMessage.toString() : null;
                }
            }
            return this[defaultMessage];
        } catch (e){
            return "Exception occurred trying to extract Java exception message: " + e;
        }
    } } });
} else {
    JavaError = Error;
}
let $rt_javaException = e => e instanceof Error && typeof e[$rt_javaExceptionProp] === 'object' ? e[$rt_javaExceptionProp] : null,
$rt_wrapException = err => {
    let ex = err[$rt_javaExceptionProp];
    if (!ex) {
        ex = $rt_createException($rt_str("(JavaScript) " + err.toString()));
        err[$rt_javaExceptionProp] = ex;
        ex.$jsException = err;
        $rt_fillStack(err, ex);
    }
    return ex;
},
$rt_createException = message => jl_RuntimeException__init_0(message),
$rt_throwableMessage = t => jl_Throwable_getMessage(t),
$rt_throwableCause = t => jl_Throwable_getCause(t),
$rt_stecls = () => $rt_objcls(),
$rt_createStackElement = (className, methodName, fileName, lineNumber) => {
    {
        return null;
    }
},
$rt_setStack = (e, stack) => {
},
$rt_packageData = null,
$rt_packages = data => {
    let i = 0;
    let packages = new Array(data.length);
    for (let j = 0;j < data.length;++j) {
        let prefixIndex = data[i++];
        let prefix = prefixIndex >= 0 ? packages[prefixIndex] : "";
        packages[j] = prefix + data[i++] + ".";
    }
    $rt_packageData = packages;
},
$rt_allClasses = [],
$rt_metadata = data => {
    let packages = $rt_packageData;
    let i = 0;
    while (i < data.length) {
        let cls = data[i++];
        $rt_allClasses.push(cls);
        let m = $rt_newClassMetadata();
        cls[$rt_meta] = m;
        let className = data[i++];
        m.name = className !== 0 ? className : null;
        if (m.name !== null) {
            let packageIndex = data[i++];
            if (packageIndex >= 0) {
                m.name = packages[packageIndex] + m.name;
            }
        }
        m.binaryName = "L" + m.name + ";";
        let superclass = data[i++];
        m.parent = superclass !== 0 ? superclass : null;
        m.superinterfaces = data[i++];
        if (m.parent) {
            cls.prototype = Object.create(m.parent.prototype);
        } else {
            cls.prototype = {  };
        }
        cls.prototype.constructor = cls;
        m.modifiers = data[i++];
        m.primitiveKind = 0;
        let innerClassInfo = data[i++];
        if (innerClassInfo !== 0) {
            let enclosingClass = innerClassInfo[0];
            m.enclosingClass = enclosingClass !== 0 ? enclosingClass : null;
            let declaringClass = innerClassInfo[1];
            m.declaringClass = declaringClass !== 0 ? declaringClass : null;
            let simpleName = innerClassInfo[2];
            m.simpleName = simpleName !== 0 ? simpleName : null;
        }
        let clinit = data[i++];
        m.clinit = clinit !== 0 ? () => {
            m.clinit = () => {
            };
            clinit();
        } : () => {
        };
        let virtualMethods = data[i++];
        if (virtualMethods !== 0) {
            for (let j = 0;j < virtualMethods.length;j += 2) {
                let name = virtualMethods[j];
                let func = virtualMethods[j + 1];
                if (typeof name === 'string') {
                    name = [name];
                }
                for (let k = 0;k < name.length;++k) {
                    cls.prototype[name[k]] = func;
                }
            }
        }
    }
},
$rt_enumConstantsMetadata = data => {
    let i = 0;
    while (i < data.length) {
        let cls = data[i++];
        cls[$rt_meta].enumConstants = data[i++];
    }
},
$rt_startThread = (runner, callback) => {
    let result;
    try {
        result = runner();
    } catch (e){
        result = e;
    }
    if (typeof callback !== 'undefined') {
        callback(result);
    } else if (result instanceof Error) {
        throw result;
    }
};
function jl_Object() {
    this.$id$ = 0;
}
let jl_Object__init_ = $this => {
    return;
},
jl_Object__init_0 = () => {
    let var_0 = new jl_Object();
    jl_Object__init_(var_0);
    return var_0;
},
jl_Object_getClass = $this => {
    return $rt_cls(jl_Object_getClassInfo($this));
},
jl_Object_getClassInfo = var$0 => {
    return var$0.constructor;
},
jl_Object_toString = var$0 => {
    let var$1, var$2, var$3, var$4, var$5, var$6, var$7, var$8, var$9, var$10;
    var$1 = jl_Class_getName(jl_Object_getClass(var$0));
    var$2 = var$0;
    if (!var$2.$id$)
        var$2.$id$ = $rt_nextId();
    var$3 = var$0.$id$;
    jl_Integer_$callClinit();
    if (!var$3)
        var$4 = $rt_s(0);
    else {
        if (!var$3)
            var$5 = 32;
        else {
            var$6 = 0;
            var$5 = var$3 >>> 16 | 0;
            if (var$5)
                var$6 = 16;
            else
                var$5 = var$3;
            var$7 = var$5 >>> 8 | 0;
            if (!var$7)
                var$7 = var$5;
            else
                var$6 = var$6 | 8;
            var$5 = var$7 >>> 4 | 0;
            if (!var$5)
                var$5 = var$7;
            else
                var$6 = var$6 | 4;
            var$7 = var$5 >>> 2 | 0;
            if (!var$7)
                var$7 = var$5;
            else
                var$6 = var$6 | 2;
            if (var$7 >>> 1 | 0)
                var$6 = var$6 | 1;
            var$5 = (32 - var$6 | 0) - 1 | 0;
        }
        var$5 = (((32 - var$5 | 0) + 4 | 0) - 1 | 0) / 4 | 0;
        var$8 = $rt_createCharArray(var$5);
        var$9 = var$8.data;
        var$6 = (var$5 - 1 | 0) * 4 | 0;
        var$7 = 0;
        while (var$6 >= 0) {
            var$10 = var$7 + 1 | 0;
            var$9[var$7] = jl_Character_forDigit((var$3 >>> var$6 | 0) & 15, 16);
            var$6 = var$6 - 4 | 0;
            var$7 = var$10;
        }
        var$4 = jl_String__init_0(var$8);
    }
    var$2 = jl_StringBuilder__init_();
    jl_StringBuilder_append(jl_StringBuilder_append0(jl_StringBuilder_append(var$2, var$1), 64), var$4);
    return jl_StringBuilder_toString(var$2);
},
jl_Object_clone = $this => {
    let $cls, $result, var$3;
    $cls = (jl_Object_getClass($this)).$classInfo;
    if (!$rt_isInstance($this, jl_Cloneable) && $cls[$rt_meta].itemType === null) {
        $cls = new jl_CloneNotSupportedException;
        jl_Exception__init_0($cls);
        $rt_throw($cls);
    }
    $result = otp_Platform_clone($this);
    $cls = $result;
    var$3 = $rt_nextId();
    $cls.$id$ = var$3;
    return $result;
},
ji_Serializable = $rt_classWithoutFields(0),
jl_Comparable = $rt_classWithoutFields(0),
jl_CharSequence = $rt_classWithoutFields(0);
function jl_String() {
    jl_Object.call(this);
    this.$hashCode = 0;
}
let jl_String_EMPTY_CHARS = null,
jl_String_EMPTY = null,
jl_String_CASE_INSENSITIVE_ORDER = null,
jl_String_$callClinit = () => {
    jl_String_$callClinit = $rt_eraseClinit(jl_String);
    jl_String__clinit_();
},
jl_String__init_1 = ($this, $characters) => {
    jl_String_$callClinit();
    $this.$nativeString = $rt_charArrayToString($characters.data, 0, $characters.data.length);
},
jl_String__init_0 = var_0 => {
    let var_1 = new jl_String();
    jl_String__init_1(var_1, var_0);
    return var_1;
},
jl_String__init_2 = (var$0, var$1) => {
    var$0.$nativeString = var$1;
},
jl_String__init_ = var_0 => {
    let var_1 = new jl_String();
    jl_String__init_2(var_1, var_0);
    return var_1;
},
jl_String_charAt = (var$0, var$1) => {
    let var$2;
    if (var$1 >= 0 && var$1 < var$0.$nativeString.length)
        return var$0.$nativeString.charCodeAt(var$1);
    var$2 = new jl_StringIndexOutOfBoundsException;
    jl_Exception__init_0(var$2);
    $rt_throw(var$2);
},
jl_String_isEmpty = $this => {
    return $this.$nativeString.length ? 0 : 1;
},
jl_String_isBlank = $this => {
    let $i;
    $i = 0;
    while ($i < $this.$nativeString.length) {
        if ($this.$nativeString.charCodeAt($i) != 32)
            return 0;
        $i = $i + 1 | 0;
    }
    return 1;
},
jl_String_toString = $this => {
    return $this;
},
jl_String_valueOf = $obj => {
    jl_String_$callClinit();
    return $obj === null ? $rt_s(1) : $obj.$toString();
},
jl_String_equals = ($this, $other) => {
    let $str;
    if ($this === $other)
        return 1;
    if (!($other instanceof jl_String))
        return 0;
    $str = $other;
    return $this.$nativeString !== $str.$nativeString ? 0 : 1;
},
jl_String__clinit_ = () => {
    let var$1;
    jl_String_EMPTY_CHARS = $rt_createCharArray(0);
    var$1 = new jl_String;
    jl_String_$callClinit();
    var$1.$nativeString = "";
    jl_String_EMPTY = var$1;
    jl_String_CASE_INSENSITIVE_ORDER = new jl_String$_clinit_$lambda$_118_0;
},
jlr_AnnotatedElement = $rt_classWithoutFields(0),
jlr_GenericDeclaration = $rt_classWithoutFields(0),
jlr_Type = $rt_classWithoutFields(0);
function jl_Class() {
    let a = this; jl_Object.call(a);
    a.$flags = 0;
    a.$classInfo = null;
    a.$name2 = null;
}
let jl_Class_createClass = $classInfo => {
    let var$2;
    var$2 = new jl_Class;
    var$2.$classInfo = $classInfo;
    return var$2;
},
jl_Class_toString = $this => {
    let var$1, var$2, var$3;
    var$1 = (!($this.$classInfo[$rt_meta].modifiers & 512) ? 0 : 1) ? $rt_s(2) : !(!$this.$classInfo[$rt_meta].primitiveKind ? 0 : 1) ? $rt_s(3) : $rt_s(4);
    var$2 = jl_Class_getName($this);
    var$3 = jl_StringBuilder__init_();
    jl_StringBuilder_append(jl_StringBuilder_append(var$3, var$1), var$2);
    return jl_StringBuilder_toString(var$3);
},
jl_Class_getName = $this => {
    let var$1, $metadataName, $result, $itemType, $itemName;
    var$1 = $this.$flags;
    if (!(var$1 & 1)) {
        $this.$flags = var$1 | 1;
        $metadataName = $this.$classInfo[$rt_meta].name;
        $result = $metadataName === null ? null : $rt_str($metadataName);
        if ($result === null) {
            $itemType = $this.$classInfo[$rt_meta].itemType;
            if ($itemType !== null) {
                $itemName = jl_Class_getName($rt_cls($itemType));
                if ($itemName !== null) {
                    if ($itemType[$rt_meta].itemType !== null) {
                        $metadataName = jl_StringBuilder__init_();
                        jl_StringBuilder_append(jl_StringBuilder_append0($metadataName, 91), $itemName);
                        $result = jl_StringBuilder_toString($metadataName);
                    } else {
                        $metadataName = jl_StringBuilder__init_();
                        jl_StringBuilder_append0(jl_StringBuilder_append(jl_StringBuilder_append($metadataName, $rt_s(5)), $itemName), 59);
                        $result = jl_StringBuilder_toString($metadataName);
                    }
                }
            }
        }
        $this.$name2 = $result;
    }
    return $this.$name2;
},
jl_Class_getComponentType = $this => {
    let $itemTypeInfo;
    $itemTypeInfo = $this.$classInfo[$rt_meta].itemType;
    return $itemTypeInfo === null ? null : $rt_cls($itemTypeInfo);
},
jl_Class_getSuperclass = $this => {
    return $this.$classInfo[$rt_meta].parent === null ? null : $rt_cls($this.$classInfo[$rt_meta].parent);
},
jl_Number = $rt_classWithoutFields();
function jl_Integer() {
    jl_Number.call(this);
    this.$value3 = 0;
}
let jl_Integer_TYPE = null,
jl_Integer_integerCache = null,
jl_Integer_$callClinit = () => {
    jl_Integer_$callClinit = $rt_eraseClinit(jl_Integer);
    jl_Integer__clinit_();
},
jl_Integer__init_0 = ($this, $value) => {
    jl_Integer_$callClinit();
    $this.$value3 = $value;
},
jl_Integer__init_ = var_0 => {
    let var_1 = new jl_Integer();
    jl_Integer__init_0(var_1, var_0);
    return var_1;
},
jl_Integer__clinit_ = () => {
    jl_Integer_TYPE = $rt_cls($rt_intcls);
};
function jl_AbstractStringBuilder() {
    let a = this; jl_Object.call(a);
    a.$buffer = null;
    a.$length = 0;
}
let jl_AbstractStringBuilder_insertSpace = ($this, $start, $end) => {
    let var$3, $sz, $i, var$6;
    var$3 = $this.$length;
    $sz = var$3 - $start | 0;
    var$3 = (var$3 + $end | 0) - $start | 0;
    jl_StringBuilder_ensureCapacity($this, var$3);
    $i = $sz - 1 | 0;
    while ($i >= 0) {
        var$6 = $this.$buffer.data;
        var$6[$end + $i | 0] = var$6[$start + $i | 0];
        $i = $i + (-1) | 0;
    }
    $this.$length = $this.$length + ($end - $start | 0) | 0;
},
jl_Appendable = $rt_classWithoutFields(0),
jl_StringBuilder = $rt_classWithoutFields(jl_AbstractStringBuilder),
jl_StringBuilder__init_0 = $this => {
    $this.$buffer = $rt_createCharArray(16);
},
jl_StringBuilder__init_ = () => {
    let var_0 = new jl_StringBuilder();
    jl_StringBuilder__init_0(var_0);
    return var_0;
},
jl_StringBuilder_append = ($this, $obj) => {
    let var$2, var$3, var$4, var$5, var$6, var$7;
    var$2 = $this.$length;
    var$3 = $this;
    $obj = $obj === null ? $rt_s(1) : $obj;
    var$4 = var$3;
    if (var$2 >= 0 && var$2 <= var$4.$length) {
        a: {
            if ($obj === null)
                $obj = $rt_s(1);
            else if (jl_String_isEmpty($obj))
                break a;
            var$5 = var$4.$length + $obj.$nativeString.length | 0;
            jl_StringBuilder_ensureCapacity(var$4, var$5);
            var$5 = var$4.$length - 1 | 0;
            while (var$5 >= var$2) {
                var$4.$buffer.data[var$5 + $obj.$nativeString.length | 0] = var$4.$buffer.data[var$5];
                var$5 = var$5 + (-1) | 0;
            }
            var$4.$length = var$4.$length + $obj.$nativeString.length | 0;
            var$5 = 0;
            while (var$5 < $obj.$nativeString.length) {
                var$6 = var$4.$buffer.data;
                var$7 = var$2 + 1 | 0;
                var$6[var$2] = jl_String_charAt($obj, var$5);
                var$5 = var$5 + 1 | 0;
                var$2 = var$7;
            }
        }
        return $this;
    }
    $obj = new jl_StringIndexOutOfBoundsException;
    jl_IndexOutOfBoundsException__init_0($obj);
    $rt_throw($obj);
},
jl_StringBuilder_append0 = ($this, $c) => {
    let var$2, var$3;
    var$2 = $this.$length;
    var$3 = $this;
    jl_AbstractStringBuilder_insertSpace(var$3, var$2, var$2 + 1 | 0);
    var$3.$buffer.data[var$2] = $c;
    return $this;
},
jl_StringBuilder_toString = $this => {
    let var$1, var$2, var$3, var$4, var$5;
    var$1 = new jl_String;
    var$2 = $this.$buffer;
    var$3 = var$2.data;
    var$4 = $this.$length;
    jl_String_$callClinit();
    var$5 = var$3.length;
    if (var$4 >= 0 && var$4 <= (var$5 - 0 | 0)) {
        var$1.$nativeString = $rt_charArrayToString(var$2.data, 0, var$4);
        return var$1;
    }
    var$1 = new jl_IndexOutOfBoundsException;
    jl_Exception__init_0(var$1);
    $rt_throw(var$1);
},
jl_StringBuilder_ensureCapacity = ($this, var$1) => {
    let var$2, var$3, var$4, var$5;
    var$2 = $this.$buffer.data.length;
    if (var$2 < var$1) {
        var$1 = var$2 >= 1073741823 ? 2147483647 : jl_Math_max(var$1, jl_Math_max(var$2 * 2 | 0, 5));
        var$3 = $this.$buffer.data;
        var$4 = $rt_createCharArray(var$1);
        var$5 = var$4.data;
        var$1 = jl_Math_min(var$1, var$3.length);
        var$2 = 0;
        while (var$2 < var$1) {
            var$5[var$2] = var$3[var$2];
            var$2 = var$2 + 1 | 0;
        }
        $this.$buffer = var$4;
    }
};
function jl_Throwable() {
    let a = this; jl_Object.call(a);
    a.$message = null;
    a.$cause = null;
    a.$suppressionEnabled = 0;
    a.$writableStackTrace = 0;
}
let jl_Throwable_fillInStackTrace = $this => {
    return $this;
},
jl_Throwable_initNativeException = $this => {
    $rt_fillNativeException($this);
},
jl_Throwable_getMessage = $this => {
    return $this.$message;
},
jl_Throwable_getCause = $this => {
    let var$1;
    var$1 = $this.$cause;
    if (var$1 === $this)
        var$1 = null;
    return var$1;
},
jl_Exception = $rt_classWithoutFields(jl_Throwable),
jl_Exception__init_0 = $this => {
    jl_Throwable_initNativeException($this);
    $this.$suppressionEnabled = 1;
    $this.$writableStackTrace = 1;
},
jl_Exception__init_2 = () => {
    let var_0 = new jl_Exception();
    jl_Exception__init_0(var_0);
    return var_0;
},
jl_Exception__init_1 = ($this, $message, $cause) => {
    jl_Throwable_initNativeException($this);
    $this.$suppressionEnabled = 1;
    $this.$writableStackTrace = 1;
    $this.$message = $message;
    $this.$cause = $cause;
},
jl_Exception__init_3 = (var_0, var_1) => {
    let var_2 = new jl_Exception();
    jl_Exception__init_1(var_2, var_0, var_1);
    return var_2;
},
jl_Exception__init_ = ($this, $message) => {
    jl_Throwable_initNativeException($this);
    $this.$suppressionEnabled = 1;
    $this.$writableStackTrace = 1;
    $this.$message = $message;
},
jl_Exception__init_4 = var_0 => {
    let var_1 = new jl_Exception();
    jl_Exception__init_(var_1, var_0);
    return var_1;
},
jl_RuntimeException = $rt_classWithoutFields(jl_Exception),
jl_RuntimeException__init_ = ($this, $message) => {
    jl_Exception__init_($this, $message);
},
jl_RuntimeException__init_0 = var_0 => {
    let var_1 = new jl_RuntimeException();
    jl_RuntimeException__init_(var_1, var_0);
    return var_1;
},
otrr_ReflectionInfo = $rt_classWithoutFields(),
otrr_ClassInfo = $rt_classWithoutFields(otrr_ReflectionInfo),
otrr_ClassInfo_newArrayInstance = (var$0, var$1) => {
    switch (var$0.primitiveKind) {
        default: return $rt_createArray(var$0, var$1);
    }
},
otr_StringInfo = $rt_classWithoutFields(otrr_ReflectionInfo),
cw_WebClassifyMain = $rt_classWithoutFields(),
cw_WebClassifyMain_worldMinX = 0.0,
cw_WebClassifyMain_worldMaxX = 0.0,
cw_WebClassifyMain_worldMinY = 0.0,
cw_WebClassifyMain_worldMaxY = 0.0,
cw_WebClassifyMain_RASTERIZER = null,
cw_WebClassifyMain_sceneSnapshot = null,
cw_WebClassifyMain_shadingEnabled = 0,
cw_WebClassifyMain_lastError = null,
cw_WebClassifyMain_activeClusterIndex = 0,
cw_WebClassifyMain_selectedClusterIndex = 0,
cw_WebClassifyMain_selectedMemberIndex = 0,
cw_WebClassifyMain_selectedHandleIndex = 0,
cw_WebClassifyMain_coMovingHandles = null,
cw_WebClassifyMain_coMoveClusterIndex = 0,
cw_WebClassifyMain_lastArgb = null,
cw_WebClassifyMain_lastOwners = null,
cw_WebClassifyMain_lastMembers = null,
cw_WebClassifyMain_lastWidth = 0,
cw_WebClassifyMain_lastHeight = 0,
cw_WebClassifyMain_handleXs = null,
cw_WebClassifyMain_handleYs = null,
cw_WebClassifyMain_handleClusterIndices = null,
cw_WebClassifyMain_handleRs = null,
cw_WebClassifyMain_handleGs = null,
cw_WebClassifyMain_handleBs = null,
cw_WebClassifyMain_handleVisible = null,
cw_WebClassifyMain_handleTotal = 0,
cw_WebClassifyMain_handleMemberIndices = null,
cw_WebClassifyMain_handleWithinMemberIndices = null,
cw_WebClassifyMain_overlayKinds = null,
cw_WebClassifyMain_overlayClusters = null;
let cw_WebClassifyMain_overlayMembers = null,
cw_WebClassifyMain_overlayAx = null,
cw_WebClassifyMain_overlayAy = null,
cw_WebClassifyMain_overlayBx = null,
cw_WebClassifyMain_overlayBy = null,
cw_WebClassifyMain_overlayRadius = null,
cw_WebClassifyMain_ellipseXs = null,
cw_WebClassifyMain_ellipseYs = null,
cw_WebClassifyMain_ellipseStarts = null,
cw_WebClassifyMain_overlayCount = 0,
cw_WebClassifyMain_$callClinit = () => {
    cw_WebClassifyMain_$callClinit = $rt_eraseClinit(cw_WebClassifyMain);
    cw_WebClassifyMain__clinit_();
},
cw_WebClassifyMain_main = var$1 => {
    cw_WebClassifyMain_$callClinit();
    cw_WebClassifyMain_installApi$js_body$_71();
},
cw_WebClassifyMain_clearSelection = () => {
    cw_WebClassifyMain_$callClinit();
    cw_WebClassifyMain_selectedClusterIndex = (-1);
    cw_WebClassifyMain_selectedMemberIndex = (-1);
    cw_WebClassifyMain_selectedHandleIndex = (-1);
    cw_WebClassifyMain_endHandleDrag();
},
cw_WebClassifyMain_computeHandles = $clusters => {
    let $total, $member, $cluster, $xs, var$6, $ys, var$8, $clusterIdx, var$10, $rs, var$12, $gs, var$14, $bs, var$16, $memberIdx, var$18, $withinIdx, var$20, $visible, var$22, $i, $c, $cluster_0, $color, $members, $m, $memberSelected, $h, $handle;
    cw_WebClassifyMain_$callClinit();
    $total = 0;
    $member = ju_AbstractList_iterator($clusters);
    while (ju_AbstractList$1_hasNext($member)) {
        $cluster = ju_AbstractList_iterator((ju_AbstractList$1_next($member)).$members);
        while (ju_AbstractList$1_hasNext($cluster)) {
            $total = $total + (ju_AbstractList$1_next($cluster)).$handleCount() | 0;
        }
    }
    $xs = $rt_createDoubleArray($total);
    var$6 = $xs.data;
    $ys = $rt_createDoubleArray($total);
    var$8 = $ys.data;
    $clusterIdx = $rt_createIntArray($total);
    var$10 = $clusterIdx.data;
    $rs = $rt_createDoubleArray($total);
    var$12 = $rs.data;
    $gs = $rt_createDoubleArray($total);
    var$14 = $gs.data;
    $bs = $rt_createDoubleArray($total);
    var$16 = $bs.data;
    $memberIdx = $rt_createIntArray($total);
    var$18 = $memberIdx.data;
    $withinIdx = $rt_createIntArray($total);
    var$20 = $withinIdx.data;
    $visible = $rt_createBooleanArray($total);
    var$22 = $visible.data;
    $i = 0;
    $c = 0;
    $cluster = $clusters;
    while ($c < ju_TemplateCollections$ImmutableArrayList_size($cluster)) {
        $cluster_0 = ju_TemplateCollections$ImmutableArrayList_get($cluster, $c);
        $color = $cluster_0.$color;
        $members = $cluster_0.$members;
        $m = 0;
        $clusters = $members;
        while ($m < $clusters.$size0) {
            $member = ju_ArrayList_get($clusters, $m);
            $memberSelected = $c == cw_WebClassifyMain_selectedClusterIndex && $m == cw_WebClassifyMain_selectedMemberIndex ? 1 : 0;
            $h = 0;
            while ($h < $member.$handleCount()) {
                $handle = $member.$getHandle($h);
                var$6[$i] = $handle.$x0;
                var$8[$i] = $handle.$y0;
                var$10[$i] = $c;
                var$12[$i] = $color.$r;
                var$14[$i] = $color.$g;
                var$16[$i] = $color.$b1;
                var$18[$i] = $m;
                var$20[$i] = $h;
                var$22[$i] = c_HandleVisibility_isVisible($member, $h, $memberSelected);
                $i = $i + 1 | 0;
                $h = $h + 1 | 0;
            }
            $m = $m + 1 | 0;
        }
        $c = $c + 1 | 0;
    }
    cw_WebClassifyMain_handleXs = $xs;
    cw_WebClassifyMain_handleYs = $ys;
    cw_WebClassifyMain_handleClusterIndices = $clusterIdx;
    cw_WebClassifyMain_handleRs = $rs;
    cw_WebClassifyMain_handleGs = $gs;
    cw_WebClassifyMain_handleBs = $bs;
    cw_WebClassifyMain_handleVisible = $visible;
    cw_WebClassifyMain_handleMemberIndices = $memberIdx;
    cw_WebClassifyMain_handleWithinMemberIndices = $withinIdx;
    cw_WebClassifyMain_handleTotal = $total;
},
cw_WebClassifyMain_computeOverlays = $clusters => {
    let $count, $em, $kinds, var$5, $clustersIdx, var$7, $membersIdx, var$9, $ax, var$11, $ay, var$13, $bx, var$15, $by, var$17, $radius, var$19, $starts, var$21, $polyX, $polyY, $i, $c, $cluster, $members, $m, $member, $pm, $sm, $cm, $lm, $outline, var$35, var$36, var$37, var$38, var$39, var$40, $p;
    cw_WebClassifyMain_$callClinit();
    $count = 0;
    $em = ju_AbstractList_iterator($clusters);
    while (ju_AbstractList$1_hasNext($em)) {
        $count = $count + cm_ClusterSite_size(ju_AbstractList$1_next($em)) | 0;
    }
    $kinds = $rt_createArray(jl_String, $count);
    var$5 = $kinds.data;
    $clustersIdx = $rt_createIntArray($count);
    var$7 = $clustersIdx.data;
    $membersIdx = $rt_createIntArray($count);
    var$9 = $membersIdx.data;
    $ax = $rt_createDoubleArray($count);
    var$11 = $ax.data;
    $ay = $rt_createDoubleArray($count);
    var$13 = $ay.data;
    $bx = $rt_createDoubleArray($count);
    var$15 = $bx.data;
    $by = $rt_createDoubleArray($count);
    var$17 = $by.data;
    $radius = $rt_createDoubleArray($count);
    var$19 = $radius.data;
    $starts = $rt_createIntArray($count + 1 | 0);
    var$21 = $starts.data;
    $polyX = ju_ArrayList__init_();
    $polyY = ju_ArrayList__init_();
    $i = 0;
    $c = 0;
    $cluster = $clusters;
    while ($c < ju_TemplateCollections$ImmutableArrayList_size($cluster)) {
        $members = (ju_TemplateCollections$ImmutableArrayList_get($cluster, $c)).$members;
        $m = 0;
        $members = $members;
        while ($m < $members.$size0) {
            $member = ju_ArrayList_get($members, $m);
            var$7[$i] = $c;
            var$9[$i] = $m;
            $clusters = $polyX;
            var$21[$i] = $clusters.$size0;
            if ($member instanceof cm_PointMember) {
                $pm = $member;
                var$5[$i] = $rt_s(6);
                var$11[$i] = (cm_PointMember_getHandle($pm, 0)).$x0;
                var$13[$i] = (cm_PointMember_getHandle($pm, 0)).$y0;
            } else if ($member instanceof cm_SegmentMember) {
                $sm = $member;
                var$5[$i] = $rt_s(7);
                $clusters = $sm.$a0;
                var$11[$i] = $clusters.$x0;
                var$13[$i] = $clusters.$y0;
                $clusters = $sm.$b0;
                var$15[$i] = $clusters.$x0;
                var$17[$i] = $clusters.$y0;
            } else if ($member instanceof cm_CircleMember) {
                $cm = $member;
                var$5[$i] = $rt_s(8);
                $clusters = $cm.$center;
                var$11[$i] = $clusters.$x0;
                var$13[$i] = $clusters.$y0;
                var$19[$i] = cm_CircleMember_radius($cm);
            } else if (!($member instanceof cm_EllipseMember)) {
                if (!($member instanceof cm_LineMember)) {
                    var$5[$i] = $rt_s(6);
                    var$11[$i] = ($member.$getHandle(0)).$x0;
                    var$13[$i] = ($member.$getHandle(0)).$y0;
                } else {
                    $lm = $member;
                    var$5[$i] = $rt_s(9);
                    $clusters = $lm.$a;
                    var$11[$i] = $clusters.$x0;
                    var$13[$i] = $clusters.$y0;
                    $clusters = $lm.$b;
                    var$15[$i] = $clusters.$x0;
                    var$17[$i] = $clusters.$y0;
                }
            } else {
                $em = $member;
                var$5[$i] = $rt_s(10);
                if ($em.$degenerate) {
                    ju_Collections_$callClinit();
                    $outline = ju_Collections_EMPTY_LIST;
                } else {
                    $outline = ju_ArrayList__init_0(96);
                    var$35 = 0;
                    while (var$35 < 96) {
                        var$36 = 6.283185307179586 * var$35 / 96.0;
                        var$37 = $em.$a1 * jl_Math_cos(var$36);
                        var$38 = $em.$b2 * jl_Math_sin(var$36);
                        var$36 = $em.$centerX;
                        var$39 = $em.$cos0;
                        var$36 = var$36 + var$37 * var$39;
                        var$40 = $em.$sin0;
                        $pm = cg_Vector_xy(var$36 - var$38 * var$40, $em.$centerY + var$37 * var$40 + var$38 * var$39);
                        ju_ArrayList_add($outline, $pm);
                        var$35 = var$35 + 1 | 0;
                    }
                }
                a: {
                    if (!$outline.$isEmpty()) {
                        $em = $outline.$iterator();
                        while (true) {
                            if (!$em.$hasNext())
                                break a;
                            $p = $em.$next();
                            ju_ArrayList_add($clusters, jl_Double_valueOf($p.$x0));
                            $pm = jl_Double_valueOf($p.$y0);
                            ju_ArrayList_add($polyY, $pm);
                        }
                    }
                    $clusters = $em.$focusA;
                    var$11[$i] = $clusters.$x0;
                    var$13[$i] = $clusters.$y0;
                    $clusters = $em.$focusB;
                    var$15[$i] = $clusters.$x0;
                    var$17[$i] = $clusters.$y0;
                    var$5[$i] = $rt_s(7);
                }
            }
            $i = $i + 1 | 0;
            $m = $m + 1 | 0;
        }
        $c = $c + 1 | 0;
    }
    var$21[$count] = $polyX.$size0;
    cw_WebClassifyMain_overlayKinds = $kinds;
    cw_WebClassifyMain_overlayClusters = $clustersIdx;
    cw_WebClassifyMain_overlayMembers = $membersIdx;
    cw_WebClassifyMain_overlayAx = $ax;
    cw_WebClassifyMain_overlayAy = $ay;
    cw_WebClassifyMain_overlayBx = $bx;
    cw_WebClassifyMain_overlayBy = $by;
    cw_WebClassifyMain_overlayRadius = $radius;
    cw_WebClassifyMain_ellipseStarts = $starts;
    cw_WebClassifyMain_ellipseXs = cw_WebClassifyMain_toDoubleArray($polyX);
    cw_WebClassifyMain_ellipseYs = cw_WebClassifyMain_toDoubleArray($polyY);
    cw_WebClassifyMain_overlayCount = $count;
},
cw_WebClassifyMain_toDoubleArray = $values => {
    let var$2, $out, var$4, $i;
    cw_WebClassifyMain_$callClinit();
    var$2 = $values;
    $out = $rt_createDoubleArray(var$2.$size0);
    var$4 = $out.data;
    $i = 0;
    while ($i < var$2.$size0) {
        var$4[$i] = (ju_ArrayList_get(var$2, $i)).$value;
        $i = $i + 1 | 0;
    }
    return $out;
},
cw_WebClassifyMain_endHandleDrag = () => {
    cw_WebClassifyMain_$callClinit();
    ju_ArrayList_clear(cw_WebClassifyMain_coMovingHandles);
    cw_WebClassifyMain_coMoveClusterIndex = (-1);
},
cw_WebClassifyMain_pixelToWorld = ($width, $height) => {
    let $sx, $tx, $sy, $ty, var$7, var$8, var$9, var$10, var$11, var$12, var$13, var$14, var$15, var$16;
    cw_WebClassifyMain_$callClinit();
    $sx = cw_WebClassifyMain_worldMaxX;
    $tx = cw_WebClassifyMain_worldMinX;
    $sx = ($sx - $tx) / $width;
    $sy = cw_WebClassifyMain_worldMinY;
    $ty = cw_WebClassifyMain_worldMaxY;
    $sy = ($sy - $ty) / $height;
    cg_Transformation_$callClinit();
    var$7 = cg_Transformation__init_($sx, 0.0, 0.0, 0.0, $sy, 0.0);
    var$8 = cg_Vector_xy($tx, $ty);
    var$9 = cg_Transformation__init_(1.0, 0.0, var$8.$x0, 0.0, 1.0, var$8.$y0);
    var$8 = new cg_Transformation;
    $sx = var$9.$mex;
    $tx = var$7.$mex;
    $sy = $sx * $tx;
    $ty = var$9.$mfx;
    var$10 = var$7.$mey;
    var$11 = $sy + $ty * var$10;
    var$12 = var$7.$mfx;
    var$13 = $sx * var$12;
    var$14 = var$7.$mfy;
    $sy = var$13 + $ty * var$14;
    var$13 = var$7.$tx;
    $sx = $sx * var$13;
    var$15 = var$7.$ty;
    $sx = $sx + $ty * var$15 + var$9.$tx;
    $ty = var$9.$mey;
    $tx = $ty * $tx;
    var$16 = var$9.$mfy;
    cg_Transformation__init_0(var$8, var$11, $sy, $sx, $tx + var$16 * var$10, $ty * var$12 + var$16 * var$14, $ty * var$13 + var$16 * var$15 + var$9.$ty);
    return var$8;
},
cw_WebClassifyMain__clinit_ = () => {
    let var$1;
    cw_WebClassifyMain_worldMinX = (-400.0);
    cw_WebClassifyMain_worldMaxX = 400.0;
    cw_WebClassifyMain_worldMinY = (-400.0);
    cw_WebClassifyMain_worldMaxY = 400.0;
    var$1 = new cc_DiagramRasterizer;
    var$1.$sizeYp = 0;
    var$1.$sizeXp = 0;
    cw_WebClassifyMain_RASTERIZER = var$1;
    cw_WebClassifyMain_$callClinit();
    var$1 = cm_SceneSnapshot__init_();
    cm_MetricKind_$callClinit();
    var$1.$metricKind = cm_MetricKind_MINIMUM_DISTANCE;
    cm_NeighborOrder_$callClinit();
    var$1.$neighborOrder = cm_NeighborOrder_NEAREST;
    cm_SiteMemberKind_$callClinit();
    var$1.$siteMemberKind = cm_SiteMemberKind_POINT;
    var$1.$nearestNeighborK = 1;
    cm_SceneSnapshot_setClusters(var$1, cm_DemoScenes_defaultClusters());
    cw_WebClassifyMain_sceneSnapshot = var$1;
    cw_WebClassifyMain_shadingEnabled = 0;
    cw_WebClassifyMain_lastError = $rt_s(4);
    cw_WebClassifyMain_activeClusterIndex = 0;
    cw_WebClassifyMain_selectedClusterIndex = (-1);
    cw_WebClassifyMain_selectedMemberIndex = (-1);
    cw_WebClassifyMain_selectedHandleIndex = (-1);
    cw_WebClassifyMain_coMovingHandles = ju_ArrayList__init_();
    cw_WebClassifyMain_coMoveClusterIndex = (-1);
    cw_WebClassifyMain_lastArgb = $rt_createIntArray(0);
    cw_WebClassifyMain_lastOwners = $rt_createIntArray(0);
    cw_WebClassifyMain_lastMembers = $rt_createIntArray(0);
    cw_WebClassifyMain_lastWidth = 0;
    cw_WebClassifyMain_lastHeight = 0;
    cw_WebClassifyMain_handleXs = $rt_createDoubleArray(0);
    cw_WebClassifyMain_handleYs = $rt_createDoubleArray(0);
    cw_WebClassifyMain_handleClusterIndices = $rt_createIntArray(0);
    cw_WebClassifyMain_handleRs = $rt_createDoubleArray(0);
    cw_WebClassifyMain_handleGs = $rt_createDoubleArray(0);
    cw_WebClassifyMain_handleBs = $rt_createDoubleArray(0);
    cw_WebClassifyMain_handleVisible = $rt_createBooleanArray(0);
    cw_WebClassifyMain_handleTotal = 0;
    cw_WebClassifyMain_handleMemberIndices = $rt_createIntArray(0);
    cw_WebClassifyMain_handleWithinMemberIndices = $rt_createIntArray(0);
    cw_WebClassifyMain_overlayKinds = $rt_createArray(jl_String, 0);
    cw_WebClassifyMain_overlayClusters = $rt_createIntArray(0);
    cw_WebClassifyMain_overlayMembers = $rt_createIntArray(0);
    cw_WebClassifyMain_overlayAx = $rt_createDoubleArray(0);
    cw_WebClassifyMain_overlayAy = $rt_createDoubleArray(0);
    cw_WebClassifyMain_overlayBx = $rt_createDoubleArray(0);
    cw_WebClassifyMain_overlayBy = $rt_createDoubleArray(0);
    cw_WebClassifyMain_overlayRadius = $rt_createDoubleArray(0);
    cw_WebClassifyMain_ellipseXs = $rt_createDoubleArray(0);
    cw_WebClassifyMain_ellipseYs = $rt_createDoubleArray(0);
    cw_WebClassifyMain_ellipseStarts = $rt_createIntArray(0);
    cw_WebClassifyMain_overlayCount = 0;
},
cw_WebClassifyMain_installApi$js_body$_71 = () => {
    globalThis.cvdCore = { renderFrame : function(w, h) {
        cw_WebClassifyMain_computeFrame$jsocb$_0(w, h);
        var argb = cw_WebClassifyMain_lastArgb$jsocb$_1();
        var owners = cw_WebClassifyMain_lastOwners$jsocb$_2();
        var members = cw_WebClassifyMain_lastMembers$jsocb$_3();
        var width = cw_WebClassifyMain_lastWidth$jsocb$_4();
        var height = cw_WebClassifyMain_lastHeight$jsocb$_5();
        var hx = cw_WebClassifyMain_handleXs$jsocb$_6();
        var hy = cw_WebClassifyMain_handleYs$jsocb$_7();
        var hc = cw_WebClassifyMain_handleClusters$jsocb$_8();
        var hm = cw_WebClassifyMain_handleMembers$jsocb$_9();
        var hw = cw_WebClassifyMain_handleWithin$jsocb$_10();
        var hv = cw_WebClassifyMain_handleVisibleFlags$jsocb$_11();
        var hr = cw_WebClassifyMain_handleRs$jsocb$_12();
        var hg = cw_WebClassifyMain_handleGs$jsocb$_13();
        var hb = cw_WebClassifyMain_handleBs$jsocb$_14();
        var hn = cw_WebClassifyMain_handleTotal$jsocb$_15();
        var metric = cw_WebClassifyMain_metricKindName$jsocb$_16();
        var order = cw_WebClassifyMain_neighborOrderName$jsocb$_17();
        var k = cw_WebClassifyMain_nearestNeighborK$jsocb$_18();
        var shading = cw_WebClassifyMain_shadingEnabled$jsocb$_19();
        var err = cw_WebClassifyMain_lastError$jsocb$_20();
        var clusterCount = cw_WebClassifyMain_clusterCount$jsocb$_21();
        var activeCluster = cw_WebClassifyMain_activeClusterIndex$jsocb$_22();
        var activeMembers = cw_WebClassifyMain_activeMemberCount$jsocb$_23();
        var siteKind = cw_WebClassifyMain_siteMemberKindName$jsocb$_24();
        var selCluster = cw_WebClassifyMain_selectedClusterIndex$jsocb$_25();
        var selMember = cw_WebClassifyMain_selectedMemberIndex$jsocb$_26();
        var selHandle = cw_WebClassifyMain_selectedHandleIndex$jsocb$_27();
        var names = [];
        for (var i = 0;i < clusterCount;i++) {
            names.push(cw_WebClassifyMain_clusterNameAt$jsocb$_28(i));
        }
        var oc = cw_WebClassifyMain_overlayCount$jsocb$_29();
        var ok = cw_WebClassifyMain_overlayKinds$jsocb$_30();
        var ocl = cw_WebClassifyMain_overlayClusters$jsocb$_31();
        var om = cw_WebClassifyMain_overlayMembers$jsocb$_32();
        var oax = cw_WebClassifyMain_overlayAx$jsocb$_33();
        var oay = cw_WebClassifyMain_overlayAy$jsocb$_34();
        var obx = cw_WebClassifyMain_overlayBx$jsocb$_35();
        var oby = cw_WebClassifyMain_overlayBy$jsocb$_36();
        var orad = cw_WebClassifyMain_overlayRadius$jsocb$_37();
        var ex = cw_WebClassifyMain_ellipseXs$jsocb$_38();
        var ey = cw_WebClassifyMain_ellipseYs$jsocb$_39();
        var es = cw_WebClassifyMain_ellipseStarts$jsocb$_40();
        return { argb : argb, owners : owners, members : members, width : width, height : height, handles : { x : hx, y : hy, cluster : hc, member : hm, within : hw, visible : hv, r : hr, g : hg, b : hb, n : hn }, overlays : { n : oc, kind : ok, cluster : ocl, member : om, ax : oax, ay : oay, bx : obx, by : oby, radius : orad, ellipseX : ex, ellipseY : ey, ellipseStarts : es }, scene : { metricKind : metric, neighborOrder : order, nearestNeighborK : k, shading : shading, lastError : err, clusterCount : clusterCount,
        activeClusterIndex : activeCluster, activeMemberCount : activeMembers, siteMemberKind : siteKind, selectedClusterIndex : selCluster, selectedMemberIndex : selMember, selectedHandleIndex : selHandle, clusterNames : names } };
    }, moveHandle : function(index, worldX, worldY, coMove) {
        cw_WebClassifyMain_moveHandle$jsocb$_41(index, worldX, worldY, !!coMove);
    }, beginHandleDrag : function(index) {
        cw_WebClassifyMain_beginHandleDrag$jsocb$_42(index);
    }, endHandleDrag : function() {
        cw_WebClassifyMain_endHandleDrag$jsocb$_43();
    }, setMetricKind : function(name) {
        return cw_WebClassifyMain_setMetricKindName$jsocb$_44(name);
    }, setNeighborOrder : function(name) {
        return cw_WebClassifyMain_setNeighborOrderName$jsocb$_45(name);
    }, setNearestNeighborK : function(k) {
        return cw_WebClassifyMain_setNearestNeighborK$jsocb$_46(k);
    }, setShadingEnabled : function(enabled) {
        cw_WebClassifyMain_setShadingEnabled$jsocb$_47(enabled);
    }, setWorldView : function(minX, maxX, minY, maxY) {
        cw_WebClassifyMain_setWorldView$jsocb$_48(minX, maxX, minY, maxY);
    }, setActiveClusterIndex : function(index) {
        return cw_WebClassifyMain_setActiveClusterIndex$jsocb$_49(index);
    }, setSiteMemberKind : function(name) {
        return cw_WebClassifyMain_setSiteMemberKindName$jsocb$_50(name);
    }, selectHandle : function(index) {
        cw_WebClassifyMain_selectHandle$jsocb$_51(index);
    }, cycleSelectedMember : function(delta) {
        cw_WebClassifyMain_cycleSelectedMember$jsocb$_52(delta);
    }, clearSelection : function() {
        cw_WebClassifyMain_clearSelection$jsocb$_53();
    }, addMemberAt : function(worldX, worldY) {
        return cw_WebClassifyMain_addMemberAt$jsocb$_54(worldX, worldY);
    }, removeMember : function() {
        return cw_WebClassifyMain_removeMember$jsocb$_55();
    }, addCluster : function() {
        return cw_WebClassifyMain_addCluster$jsocb$_56();
    }, removeCluster : function() {
        return cw_WebClassifyMain_removeCluster$jsocb$_57();
    }, loadSceneJson : function(json) {
        return cw_WebClassifyMain_loadSceneJson$jsocb$_58(json);
    } };
},
cw_WebClassifyMain_handleClusters$jsocb$_8 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap0(cw_WebClassifyMain_handleClusterIndices);
},
cw_WebClassifyMain_overlayKinds$jsocb$_30 = () => {
    let var$1, var$2, var$3, var$4;
    cw_WebClassifyMain_$callClinit();
    var$1 = cw_WebClassifyMain_overlayKinds;
    if (var$1 === null)
        var$2 = null;
    else {
        var$1 = var$1.data;
        var$3 = var$1.length;
        var$2 = new Array(var$3);
        var$4 = 0;
        while (var$4 < var$3) {
            var$2[var$4] = $rt_ustr(var$1[var$4]);
            var$4 = var$4 + 1 | 0;
        }
    }
    return var$2;
},
cw_WebClassifyMain_overlayRadius$jsocb$_37 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_overlayRadius);
},
cw_WebClassifyMain_addCluster$jsocb$_56 = () => {
    let var$1, var$2, var$3, var$4, var$5, var$6, var$7, var$8, var$9, var$10, var$11, var$12, var$13, var$14, var$15, var$16, var$17, var$18;
    cw_WebClassifyMain_$callClinit();
    var$1 = cw_WebClassifyMain_sceneSnapshot;
    var$1 = cm_MetricMemberCompatibility_invalidNewMemberMessage(var$1.$metricKind, var$1.$siteMemberKind);
    if (ju_Optional_isPresent(var$1)) {
        var$1 = ju_Optional_get(var$1);
        cw_WebClassifyMain_lastError = var$1;
    } else {
        var$1 = cw_WebClassifyMain_sceneSnapshot.$clusters0;
        var$2 = var$1.$size0;
        if (var$2 >= 32) {
            var$1 = $rt_s(11);
            cw_WebClassifyMain_lastError = var$1;
        } else {
            var$3 = (360 * var$2 | 0) * 0.618033988749895 % 360.0;
            cm_Rgba_$callClinit();
            var$3 = var$3 % 360.0;
            var$4 = var$3 + 360.0;
            var$5 = var$4 % 360.0;
            var$6 = cm_Rgba_clamp01(0.65);
            var$7 = cm_Rgba_clamp01(0.95);
            var$8 = var$7 * var$6;
            var$9 = var$8 * (1.0 - jl_Math_abs(var$5 / 60.0 % 2.0 - 1.0));
            var$7 = var$7 - var$8;
            if (var$5 < 60.0)
                var$6 = 0.0;
            else if (var$5 < 120.0) {
                var$6 = 0.0;
                var$5 = var$8;
                var$8 = var$9;
                var$9 = var$5;
            } else if (var$5 < 180.0) {
                var$10 = var$8;
                var$6 = var$9;
                var$8 = 0.0;
                var$9 = var$10;
            } else if (var$5 < 240.0) {
                var$6 = var$8;
                var$8 = 0.0;
            } else if (!(var$5 < 300.0)) {
                var$6 = var$9;
                var$9 = 0.0;
            } else {
                var$6 = var$8;
                var$8 = var$9;
                var$9 = 0.0;
            }
            var$11 = cm_Rgba__init_(var$8 + var$7, var$9 + var$7, var$6 + var$7, 1.0);
            var$12 = cg_Vector_xy((-280) + ((var$2 % 5 | 0) * 140 | 0) | 0, (-200) + ((var$2 / 5 | 0) * 140 | 0) | 0);
            var$12 = cm_SiteMemberFactory_createDefault(cw_WebClassifyMain_sceneSnapshot.$siteMemberKind, var$2, 0, var$12);
            var$13 = new cm_ClusterSite;
            cm_ClusterNaming_$callClinit();
            if (!(var$3 < 0.0))
                var$4 = var$3;
            var$14 = 0;
            var$9 = Infinity;
            var$15 = 0;
            while (true) {
                var$16 = cm_ClusterNaming_HUE_CENTERS.data;
                if (var$15 >= var$16.length)
                    break;
                var$3 = jl_Math_abs(var$4 - var$16[var$15]) % 360.0;
                if (var$3 > 180.0)
                    var$3 = 360.0 - var$3;
                if (var$3 < var$9) {
                    var$14 = var$15;
                    var$9 = var$3;
                }
                var$15 = var$15 + 1 | 0;
            }
            var$17 = cm_ClusterNaming_NAMES.data[var$14];
            ju_Collections_$callClinit();
            var$18 = new ju_TemplateCollections$SingleElementList;
            jl_Object__init_(var$18);
            var$18.$value1 = var$12;
            cm_ClusterSite__init_(var$13, var$17, var$11, var$18);
            ju_ArrayList_add(var$1, var$13);
            cw_WebClassifyMain_activeClusterIndex = var$2;
            cw_WebClassifyMain_selectedClusterIndex = var$2;
            cw_WebClassifyMain_selectedMemberIndex = 0;
            cw_WebClassifyMain_selectedHandleIndex = c_HandleVisibility_primaryHandleIndex(ju_ArrayList_get((ju_ArrayList_get(cw_WebClassifyMain_sceneSnapshot.$clusters0, var$2)).$members, 0));
            cw_WebClassifyMain_lastError = $rt_s(4);
            var$1 = $rt_s(4);
        }
    }
    return $rt_ustr(var$1);
},
cw_WebClassifyMain_removeCluster$jsocb$_57 = () => {
    let var$1, var$2, var$3;
    cw_WebClassifyMain_$callClinit();
    var$1 = cw_WebClassifyMain_sceneSnapshot.$clusters0;
    if (var$1.$size0 <= 1) {
        var$1 = $rt_s(12);
        cw_WebClassifyMain_lastError = var$1;
    } else {
        ju_ArrayList_remove(var$1, cw_WebClassifyMain_activeClusterIndex);
        var$2 = cw_WebClassifyMain_activeClusterIndex;
        var$3 = cw_WebClassifyMain_sceneSnapshot.$clusters0.$size0;
        if (var$2 >= var$3)
            cw_WebClassifyMain_activeClusterIndex = var$3 - 1 | 0;
        cw_WebClassifyMain_selectedClusterIndex = (-1);
        cw_WebClassifyMain_selectedMemberIndex = (-1);
        cw_WebClassifyMain_selectedHandleIndex = (-1);
        cw_WebClassifyMain_lastError = $rt_s(4);
        var$1 = $rt_s(4);
    }
    return $rt_ustr(var$1);
},
cw_WebClassifyMain_lastError$jsocb$_20 = () => {
    cw_WebClassifyMain_$callClinit();
    return $rt_ustr(cw_WebClassifyMain_lastError);
},
cw_WebClassifyMain_ellipseStarts$jsocb$_40 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap0(cw_WebClassifyMain_ellipseStarts);
},
cw_WebClassifyMain_overlayMembers$jsocb$_32 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap0(cw_WebClassifyMain_overlayMembers);
},
cw_WebClassifyMain_removeMember$jsocb$_55 = () => {
    let var$1, var$2, var$3, var$4;
    cw_WebClassifyMain_$callClinit();
    if (ju_AbstractCollection_isEmpty(cw_WebClassifyMain_sceneSnapshot.$clusters0)) {
        var$1 = $rt_s(13);
        cw_WebClassifyMain_lastError = var$1;
    } else {
        var$2 = cw_WebClassifyMain_selectedClusterIndex;
        if (var$2 < 0)
            var$2 = cw_WebClassifyMain_activeClusterIndex;
        var$3 = cw_WebClassifyMain_selectedMemberIndex;
        var$1 = ju_ArrayList_get(cw_WebClassifyMain_sceneSnapshot.$clusters0, var$2);
        if (!(var$3 >= 0 && var$3 < cm_ClusterSite_size(var$1)))
            var$3 = cm_ClusterSite_size(var$1) - 1 | 0;
        if (cm_ClusterSite_size(var$1) <= 1) {
            var$1 = $rt_s(14);
            cw_WebClassifyMain_lastError = var$1;
        } else {
            ju_ArrayList_remove(var$1.$members, var$3);
            cw_WebClassifyMain_selectedClusterIndex = var$2;
            var$4 = jl_Math_min(var$3, cm_ClusterSite_size(var$1) - 1 | 0);
            cw_WebClassifyMain_selectedMemberIndex = var$4;
            cw_WebClassifyMain_selectedHandleIndex = c_HandleVisibility_primaryHandleIndex(ju_ArrayList_get(var$1.$members, var$4));
            cw_WebClassifyMain_activeClusterIndex = var$2;
            cw_WebClassifyMain_lastError = $rt_s(4);
            var$1 = $rt_s(4);
        }
    }
    return $rt_ustr(var$1);
},
cw_WebClassifyMain_activeClusterIndex$jsocb$_22 = () => {
    cw_WebClassifyMain_$callClinit();
    return cw_WebClassifyMain_activeClusterIndex;
},
cw_WebClassifyMain_setNearestNeighborK$jsocb$_46 = var$1 => {
    let var$2;
    cw_WebClassifyMain_$callClinit();
    var$2 = var$1;
    if (var$2 >= 1 && var$2 <= 32) {
        cw_WebClassifyMain_sceneSnapshot.$nearestNeighborK = var$2;
        cw_WebClassifyMain_lastError = $rt_s(4);
        var$1 = $rt_s(4);
    } else {
        var$1 = $rt_s(15);
        cw_WebClassifyMain_lastError = var$1;
    }
    return $rt_ustr(var$1);
},
cw_WebClassifyMain_lastOwners$jsocb$_2 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap0(cw_WebClassifyMain_lastOwners);
},
cw_WebClassifyMain_handleVisibleFlags$jsocb$_11 = () => {
    let var$1, var$2, var$3, var$4;
    cw_WebClassifyMain_$callClinit();
    var$1 = cw_WebClassifyMain_handleVisible;
    if (var$1 === null)
        var$2 = null;
    else {
        var$1 = var$1.data;
        var$3 = var$1.length;
        var$2 = new Array(var$3);
        var$4 = 0;
        while (var$4 < var$3) {
            var$2[var$4] = !!var$1[var$4];
            var$4 = var$4 + 1 | 0;
        }
    }
    return var$2;
},
cw_WebClassifyMain_activeMemberCount$jsocb$_23 = () => {
    let var$1, var$2;
    cw_WebClassifyMain_$callClinit();
    if (ju_AbstractCollection_isEmpty(cw_WebClassifyMain_sceneSnapshot.$clusters0))
        var$1 = 0;
    else {
        var$2 = cw_WebClassifyMain_sceneSnapshot.$clusters0;
        var$1 = cw_WebClassifyMain_activeClusterIndex;
        var$1 = cm_ClusterSite_size(ju_ArrayList_get(var$2, var$1));
    }
    return var$1;
},
cw_WebClassifyMain_lastHeight$jsocb$_5 = () => {
    cw_WebClassifyMain_$callClinit();
    return cw_WebClassifyMain_lastHeight;
},
cw_WebClassifyMain_addMemberAt$jsocb$_54 = (var$1, var$2) => {
    let var$3, var$4, var$5;
    cw_WebClassifyMain_$callClinit();
    var$3 = var$1;
    var$4 = var$2;
    var$1 = cw_WebClassifyMain_sceneSnapshot;
    var$1 = cm_MetricMemberCompatibility_invalidNewMemberMessage(var$1.$metricKind, var$1.$siteMemberKind);
    if (ju_Optional_isPresent(var$1)) {
        var$1 = ju_Optional_get(var$1);
        cw_WebClassifyMain_lastError = var$1;
    } else if (ju_AbstractCollection_isEmpty(cw_WebClassifyMain_sceneSnapshot.$clusters0)) {
        var$1 = $rt_s(16);
        cw_WebClassifyMain_lastError = var$1;
    } else {
        var$1 = cw_WebClassifyMain_sceneSnapshot.$clusters0;
        var$5 = cw_WebClassifyMain_activeClusterIndex;
        var$1 = ju_ArrayList_get(var$1, var$5);
        if (cm_ClusterSite_size(var$1) >= 32) {
            var$1 = $rt_s(17);
            cw_WebClassifyMain_lastError = var$1;
        } else {
            var$2 = cm_SiteMemberFactory_createDefault(cw_WebClassifyMain_sceneSnapshot.$siteMemberKind, cw_WebClassifyMain_activeClusterIndex, cm_ClusterSite_size(var$1), cg_Vector_xy(var$3, var$4));
            ju_ArrayList_add(var$1.$members, var$2);
            cw_WebClassifyMain_selectedClusterIndex = cw_WebClassifyMain_activeClusterIndex;
            cw_WebClassifyMain_selectedMemberIndex = cm_ClusterSite_size(var$1) - 1 | 0;
            cw_WebClassifyMain_selectedHandleIndex = c_HandleVisibility_primaryHandleIndex(var$2);
            cw_WebClassifyMain_lastError = $rt_s(4);
            var$1 = $rt_s(4);
        }
    }
    return $rt_ustr(var$1);
},
cw_WebClassifyMain_setNeighborOrderName$jsocb$_45 = var$1 => {
    let var$2, $$je;
    cw_WebClassifyMain_$callClinit();
    var$1 = $rt_str(var$1);
    a: {
        try {
            cm_SceneSnapshot_setNeighborOrder(cw_WebClassifyMain_sceneSnapshot, cm_NeighborOrder_valueOf(var$1));
            cw_WebClassifyMain_lastError = $rt_s(4);
            var$2 = $rt_s(4);
            break a;
        } catch ($$e) {
            $$je = $rt_wrapException($$e);
            if ($$je instanceof jl_IllegalArgumentException) {
                var$2 = jl_StringBuilder__init_();
                jl_StringBuilder_append(jl_StringBuilder_append(var$2, $rt_s(18)), var$1);
                var$2 = jl_StringBuilder_toString(var$2);
                cw_WebClassifyMain_lastError = var$2;
                break a;
            } else {
                throw $$e;
            }
        }
    }
    return $rt_ustr(var$2);
},
cw_WebClassifyMain_lastMembers$jsocb$_3 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap0(cw_WebClassifyMain_lastMembers);
},
cw_WebClassifyMain_overlayClusters$jsocb$_31 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap0(cw_WebClassifyMain_overlayClusters);
},
cw_WebClassifyMain_setWorldView$jsocb$_48 = (var$1, var$2, var$3, var$4) => {
    let var$5, var$6, var$7, var$8;
    cw_WebClassifyMain_$callClinit();
    var$5 = var$1;
    var$6 = var$2;
    var$7 = var$3;
    var$8 = var$4;
    if (var$6 > var$5 && var$8 > var$7) {
        cw_WebClassifyMain_worldMinX = var$5;
        cw_WebClassifyMain_worldMaxX = var$6;
        cw_WebClassifyMain_worldMinY = var$7;
        cw_WebClassifyMain_worldMaxY = var$8;
        cw_WebClassifyMain_lastError = $rt_s(4);
    } else
        cw_WebClassifyMain_lastError = $rt_s(19);
},
cw_WebClassifyMain_setShadingEnabled$jsocb$_47 = var$1 => {
    cw_WebClassifyMain_$callClinit();
    cw_WebClassifyMain_shadingEnabled = var$1 ? 1 : 0;
},
cw_WebClassifyMain_lastArgb$jsocb$_1 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap0(cw_WebClassifyMain_lastArgb);
},
cw_WebClassifyMain_overlayBx$jsocb$_35 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_overlayBx);
},
cw_WebClassifyMain_selectedMemberIndex$jsocb$_26 = () => {
    cw_WebClassifyMain_$callClinit();
    return cw_WebClassifyMain_selectedMemberIndex;
},
cw_WebClassifyMain_cycleSelectedMember$jsocb$_52 = var$1 => {
    let var$2, var$3, var$4, var$5;
    cw_WebClassifyMain_$callClinit();
    var$2 = var$1;
    if (!ju_AbstractCollection_isEmpty(cw_WebClassifyMain_sceneSnapshot.$clusters0)) {
        var$3 = cw_WebClassifyMain_activeClusterIndex;
        if (!(var$3 >= 0 && var$3 < cw_WebClassifyMain_sceneSnapshot.$clusters0.$size0))
            cw_WebClassifyMain_activeClusterIndex = 0;
        var$1 = cw_WebClassifyMain_sceneSnapshot.$clusters0;
        var$3 = cw_WebClassifyMain_activeClusterIndex;
        var$1 = ju_ArrayList_get(var$1, var$3);
        var$3 = cm_ClusterSite_size(var$1);
        if (var$3 > 0) {
            a: {
                var$4 = cw_WebClassifyMain_selectedClusterIndex;
                var$5 = cw_WebClassifyMain_activeClusterIndex;
                if (var$4 == var$5) {
                    var$4 = cw_WebClassifyMain_selectedMemberIndex;
                    if (var$4 >= 0)
                        break a;
                }
                var$4 = var$2 <= 0 ? 0 : (-1);
            }
            var$2 = var$4 + var$2 | 0;
            var$4 = var$2 % var$3 | 0;
            if ((var$2 ^ var$3) < 0 && var$4)
                var$4 = var$4 + var$3 | 0;
            cw_WebClassifyMain_selectedClusterIndex = var$5;
            cw_WebClassifyMain_selectedMemberIndex = var$4;
            cw_WebClassifyMain_selectedHandleIndex = c_HandleVisibility_primaryHandleIndex(ju_ArrayList_get(var$1.$members, var$4));
        }
    }
},
cw_WebClassifyMain_ellipseXs$jsocb$_38 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_ellipseXs);
},
cw_WebClassifyMain_handleXs$jsocb$_6 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_handleXs);
},
cw_WebClassifyMain_handleBs$jsocb$_14 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_handleBs);
},
cw_WebClassifyMain_beginHandleDrag$jsocb$_42 = var$1 => {
    let var$2, var$3, var$4, var$5, var$6, var$7, var$8, var$9, var$10, var$11, var$12, var$13;
    cw_WebClassifyMain_$callClinit();
    var$2 = var$1;
    ju_ArrayList_clear(cw_WebClassifyMain_coMovingHandles);
    cw_WebClassifyMain_coMoveClusterIndex = (-1);
    if (var$2 >= 0 && var$2 < cw_WebClassifyMain_handleTotal) {
        var$3 = cw_WebClassifyMain_handleClusterIndices.data[var$2];
        var$4 = cw_WebClassifyMain_handleMemberIndices.data[var$2];
        var$2 = cw_WebClassifyMain_handleWithinMemberIndices.data[var$2];
        var$1 = ju_ArrayList_get(cw_WebClassifyMain_sceneSnapshot.$clusters0, var$3);
        var$5 = (ju_ArrayList_get(var$1.$members, var$4)).$getHandle(var$2);
        cw_WebClassifyMain_coMoveClusterIndex = var$3;
        var$6 = 0;
        while (var$6 < cm_ClusterSite_size(var$1)) {
            var$7 = ju_ArrayList_get(var$1.$members, var$6);
            var$8 = $rt_compare(var$6, var$4);
            var$9 = var$8 ? 0 : 1;
            var$10 = 0;
            while (var$10 < var$7.$handleCount()) {
                if (!(!var$8 && var$10 == var$2) && c_HandleVisibility_isVisible(var$7, var$10, var$9)) {
                    var$11 = var$7.$getHandle(var$10);
                    if (var$11 === var$5)
                        var$3 = 1;
                    else if (!(var$5 instanceof cg_Vector))
                        var$3 = 0;
                    else {
                        var$12 = var$5;
                        var$3 = var$11.$x0 === var$12.$x0 && var$11.$y0 === var$12.$y0 ? 1 : 0;
                    }
                    if (var$3) {
                        var$12 = cw_WebClassifyMain_coMovingHandles;
                        var$13 = $rt_createIntArrayFromData([var$6, var$10]);
                        ju_ArrayList_add(var$12, var$13);
                    }
                }
                var$10 = var$10 + 1 | 0;
            }
            var$6 = var$6 + 1 | 0;
        }
    }
},
cw_WebClassifyMain_selectedHandleIndex$jsocb$_27 = () => {
    cw_WebClassifyMain_$callClinit();
    return cw_WebClassifyMain_selectedHandleIndex;
};
let cw_WebClassifyMain_nearestNeighborK$jsocb$_18 = () => {
    cw_WebClassifyMain_$callClinit();
    return cw_WebClassifyMain_sceneSnapshot.$nearestNeighborK;
},
cw_WebClassifyMain_moveHandle$jsocb$_41 = (var$1, var$2, var$3, var$4) => {
    let var$5, var$6, var$7, var$8, var$9, var$10, var$11, var$12;
    cw_WebClassifyMain_$callClinit();
    var$5 = var$1;
    var$6 = var$2;
    var$7 = var$3;
    var$8 = var$4 ? 1 : 0;
    if (var$5 >= 0 && var$5 < cw_WebClassifyMain_handleTotal) {
        a: {
            var$9 = cw_WebClassifyMain_handleClusterIndices.data[var$5];
            var$10 = cw_WebClassifyMain_handleMemberIndices.data[var$5];
            var$5 = cw_WebClassifyMain_handleWithinMemberIndices.data[var$5];
            var$3 = ju_ArrayList_get(cw_WebClassifyMain_sceneSnapshot.$clusters0, var$9);
            var$1 = ju_ArrayList_get(var$3.$members, var$10);
            var$4 = cg_Vector_xy(var$6, var$7);
            cm_ClusterSite_setMember(var$3, var$10, var$1.$withHandle(var$5, var$4));
            if (var$8 && cw_WebClassifyMain_coMoveClusterIndex == var$9) {
                var$2 = ju_AbstractList_iterator(cw_WebClassifyMain_coMovingHandles);
                while (true) {
                    if (!ju_AbstractList$1_hasNext(var$2))
                        break a;
                    var$11 = (ju_AbstractList$1_next(var$2)).data;
                    var$12 = var$11[0];
                    var$8 = var$11[1];
                    cm_ClusterSite_setMember(var$3, var$12, (ju_ArrayList_get(var$3.$members, var$12)).$withHandle(var$8, var$4));
                }
            }
        }
        cw_WebClassifyMain_selectedClusterIndex = var$9;
        cw_WebClassifyMain_selectedMemberIndex = var$10;
        cw_WebClassifyMain_selectedHandleIndex = var$5;
        cw_WebClassifyMain_activeClusterIndex = var$9;
    }
},
cw_WebClassifyMain_clusterCount$jsocb$_21 = () => {
    cw_WebClassifyMain_$callClinit();
    return cw_WebClassifyMain_sceneSnapshot.$clusters0.$size0;
},
cw_WebClassifyMain_handleGs$jsocb$_13 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_handleGs);
},
cw_WebClassifyMain_overlayAy$jsocb$_34 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_overlayAy);
},
cw_WebClassifyMain_loadSceneJson$jsocb$_58 = var$1 => {
    let $$je;
    cw_WebClassifyMain_$callClinit();
    var$1 = $rt_str(var$1);
    a: {
        if (var$1 !== null && !jl_String_isBlank(var$1))
            try {
                cw_WebClassifyMain_sceneSnapshot = cw_SceneJsonJs_parse(var$1);
                cw_WebClassifyMain_activeClusterIndex = 0;
                cw_WebClassifyMain_clearSelection();
                cw_WebClassifyMain_lastError = $rt_s(4);
                var$1 = $rt_s(4);
                break a;
            } catch ($$e) {
                $$je = $rt_wrapException($$e);
                if ($$je instanceof ci_SceneJsonException) {
                    var$1 = $$je;
                    var$1 = var$1.$message;
                    if (var$1 === null)
                        var$1 = $rt_s(20);
                    cw_WebClassifyMain_lastError = var$1;
                    break a;
                } else {
                    throw $$e;
                }
            }
        var$1 = $rt_s(21);
        cw_WebClassifyMain_lastError = var$1;
    }
    return $rt_ustr(var$1);
},
cw_WebClassifyMain_metricKindName$jsocb$_16 = () => {
    cw_WebClassifyMain_$callClinit();
    return $rt_ustr(cw_WebClassifyMain_sceneSnapshot.$metricKind.$name0);
},
cw_WebClassifyMain_selectedClusterIndex$jsocb$_25 = () => {
    cw_WebClassifyMain_$callClinit();
    return cw_WebClassifyMain_selectedClusterIndex;
},
cw_WebClassifyMain_ellipseYs$jsocb$_39 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_ellipseYs);
},
cw_WebClassifyMain_handleWithin$jsocb$_10 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap0(cw_WebClassifyMain_handleWithinMemberIndices);
},
cw_WebClassifyMain_handleYs$jsocb$_7 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_handleYs);
},
cw_WebClassifyMain_siteMemberKindName$jsocb$_24 = () => {
    cw_WebClassifyMain_$callClinit();
    return $rt_ustr(cw_WebClassifyMain_sceneSnapshot.$siteMemberKind.$name0);
},
cw_WebClassifyMain_overlayAx$jsocb$_33 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_overlayAx);
},
cw_WebClassifyMain_setActiveClusterIndex$jsocb$_49 = var$1 => {
    let var$2;
    cw_WebClassifyMain_$callClinit();
    var$2 = var$1;
    if (var$2 >= 0 && var$2 < cw_WebClassifyMain_sceneSnapshot.$clusters0.$size0) {
        if (cw_WebClassifyMain_activeClusterIndex != var$2) {
            cw_WebClassifyMain_activeClusterIndex = var$2;
            cw_WebClassifyMain_clearSelection();
        }
        cw_WebClassifyMain_lastError = $rt_s(4);
        var$1 = $rt_s(4);
    } else {
        var$1 = $rt_s(22);
        cw_WebClassifyMain_lastError = var$1;
    }
    return $rt_ustr(var$1);
},
cw_WebClassifyMain_neighborOrderName$jsocb$_17 = () => {
    cw_WebClassifyMain_$callClinit();
    return $rt_ustr(cw_WebClassifyMain_sceneSnapshot.$neighborOrder.$name0);
},
cw_WebClassifyMain_handleRs$jsocb$_12 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_handleRs);
},
cw_WebClassifyMain_shadingEnabled$jsocb$_19 = () => {
    cw_WebClassifyMain_$callClinit();
    return !!cw_WebClassifyMain_shadingEnabled;
},
cw_WebClassifyMain_setMetricKindName$jsocb$_44 = var$1 => {
    let var$2, $$je;
    cw_WebClassifyMain_$callClinit();
    var$1 = $rt_str(var$1);
    a: {
        b: {
            try {
                var$2 = cm_MetricKind_valueOf(var$1);
                break b;
            } catch ($$e) {
                $$je = $rt_wrapException($$e);
                if ($$je instanceof jl_IllegalArgumentException) {
                } else {
                    throw $$e;
                }
            }
            var$2 = jl_StringBuilder__init_();
            jl_StringBuilder_append(jl_StringBuilder_append(var$2, $rt_s(23)), var$1);
            var$1 = jl_StringBuilder_toString(var$2);
            cw_WebClassifyMain_lastError = var$1;
            break a;
        }
        var$1 = cm_MetricMemberCompatibility_invalidMetricMessage(var$2, cw_WebClassifyMain_sceneSnapshot.$clusters0);
        if (ju_Optional_isPresent(var$1)) {
            var$1 = ju_Optional_get(var$1);
            cw_WebClassifyMain_lastError = var$1;
        } else {
            cw_WebClassifyMain_sceneSnapshot.$metricKind = var$2;
            cw_WebClassifyMain_lastError = $rt_s(4);
            var$1 = $rt_s(4);
        }
    }
    return $rt_ustr(var$1);
},
cw_WebClassifyMain_computeFrame$jsocb$_0 = (var$1, var$2) => {
    let var$3, var$4, var$5, var$6, var$7, var$8, var$9, var$10, var$11, var$12, var$13, var$14, var$15, var$16;
    cw_WebClassifyMain_$callClinit();
    var$3 = var$1;
    var$4 = var$2;
    if (var$3 >= 1 && var$4 >= 1) {
        var$1 = cw_WebClassifyMain_sceneSnapshot;
        var$5 = var$1.$clusters0;
        var$6 = var$1.$metricKind;
        var$7 = var$1.$neighborOrder;
        var$8 = var$1.$nearestNeighborK;
        cc_ScenePreparation_$callClinit();
        var$9 = new cc_ScenePreparation$PreparedScene;
        var$10 = new ju_TemplateCollections$ImmutableArrayList;
        var$11 = $rt_createArray(jl_Object, var$5.$size0);
        var$12 = var$11.data;
        var$1 = ju_AbstractList_iterator(var$5);
        var$13 = 0;
        var$2 = var$1;
        while (true) {
            if (!ju_AbstractList$1_hasNext(var$2)) {
                a: {
                    var$10.$list = var$11;
                    cc_ScenePreparation$1_$callClinit();
                    switch (cc_ScenePreparation$1_$SwitchMap$cvdexplorer$metric$MetricKind.data[var$6.$ordinal]) {
                        case 1:
                            var$2 = cc_ScenePreparation_MINIMUM_DISTANCE;
                            break a;
                        case 2:
                            var$2 = cc_ScenePreparation_MAXIMUM_DISTANCE;
                            break a;
                        case 3:
                            var$2 = cc_ScenePreparation_SUM_OF_DISTANCES;
                            break a;
                        case 4:
                            var$2 = cc_ScenePreparation_MEAN_DISTANCE;
                            break a;
                        case 5:
                            var$1 = var$5;
                            var$2 = new jusi_StreamOverSpliterator;
                            var$6 = new jusi_SpliteratorOverCollection;
                            var$6.$collection = var$1;
                            var$2.$spliterator = var$6;
                            var$1 = new cc_ScenePreparation$metricFor$lambda$_2_0;
                            var$2 = var$2;
                            var$6 = new jusi_MappingToIntStreamImpl;
                            var$6.$source = var$2;
                            var$6.$mapper = var$1;
                            var$1 = var$6;
                            var$2 = new jusi_SimpleIntStreamImpl$min$lambda$_20_0;
                            var$6 = new jusi_ReducingIntConsumer;
                            var$6.$accumulator = var$2;
                            var$6.$result = 0;
                            var$6.$initialized = 0;
                            var$1 = var$1;
                            while (true) {
                                var$2 = var$1.$source;
                                var$5 = new jusi_MappingToIntStreamImpl$next$lambda$_1_0;
                                var$5.$_01 = var$1;
                                var$5.$_1 = var$6;
                                var$2 = var$2;
                                var$14 = new jusi_StreamOverSpliterator$AdapterAction;
                                jl_Object__init_(var$14);
                                var$14.$consumer = var$5;
                                b: {
                                    while (true) {
                                        var$5 = var$2.$spliterator;
                                        jusi_SpliteratorOverCollection_ensureIterator(var$5);
                                        if (!ju_AbstractList$1_hasNext(var$5.$iterator0))
                                            var$15 = 0;
                                        else {
                                            var$5 = ju_AbstractList$1_next(var$5.$iterator0);
                                            jusi_StreamOverSpliterator$AdapterAction_accept(var$14, var$5);
                                            var$15 = 1;
                                        }
                                        if (!var$15) {
                                            var$15 = 0;
                                            break b;
                                        }
                                        if (var$14.$wantsMore)
                                            continue;
                                        else
                                            break;
                                    }
                                    var$15 = 1;
                                }
                                if (!var$15)
                                    break;
                            }
                            if (var$6.$initialized)
                                var$2 = ju_OptionalInt__init_(var$6.$result);
                            else {
                                if (ju_OptionalInt_emptyInstance === null)
                                    ju_OptionalInt_emptyInstance = ju_OptionalInt__init_(0);
                                var$2 = ju_OptionalInt_emptyInstance;
                            }
                            var$15 = 0;
                            if (var$2 !== ju_OptionalInt_emptyInstance)
                                var$15 = var$2.$value2;
                            var$15 = var$15 < 1 ? 1 : jl_Math_max(1, jl_Math_min(var$8, var$15));
                            var$2 = new cm_KthNearestPointDistanceMetric;
                            var$2.$k = var$15;
                            break a;
                        default:
                    }
                    var$2 = new jl_MatchException;
                    jl_Exception__init_1(var$2, null, null);
                    $rt_throw(var$2);
                }
                c: {
                    switch (cc_ScenePreparation$1_$SwitchMap$cvdexplorer$model$NeighborOrder.data[var$7.$ordinal]) {
                        case 1:
                            var$1 = cc_ScenePreparation_NEAREST_OWNERSHIP;
                            break c;
                        case 2:
                            var$1 = cc_ScenePreparation_FARTHEST_OWNERSHIP;
                            break c;
                        default:
                    }
                    var$6 = new jl_MatchException;
                    jl_Exception__init_1(var$6, null, null);
                    $rt_throw(var$6);
                }
                var$9.$clusters2 = var$10;
                var$9.$metric = var$2;
                var$9.$ownershipSelector = var$1;
                var$1 = new cr_ClusterColorizer;
                var$2 = cm_Rgba_gray(0.92);
                var$15 = cw_WebClassifyMain_shadingEnabled;
                var$1.$clusters3 = var$10;
                var$1.$background = var$2;
                var$1.$shadingEnabled = var$15;
                cg_Vector_$callClinit();
                var$2 = cg_Box_pq(cg_Vector_ZERO, cg_Vector_xy(var$3, var$4));
                var$16 = cg_Box_pq(cg_Vector_xy(jl_Math_min0(var$2.$px, var$2.$qx), jl_Math_min0(var$2.$py, var$2.$qy)), cg_Vector_xy(jl_Math_max0(var$2.$px, var$2.$qx), jl_Math_max0(var$2.$py, var$2.$qy)));
                var$2 = cw_WebClassifyMain_pixelToWorld(var$3, var$4);
                var$6 = cw_WebClassifyMain_RASTERIZER;
                var$5 = new cw_WebClassifyMain$computeFrame$lambda$_2_0;
                var$5.$_0 = var$9;
                ju_Objects_requireNonNull(var$1);
                var$14 = new cw_WebClassifyMain$computeFrame$lambda$_2_1;
                var$14.$_00 = var$1;
                var$1 = cc_DiagramRasterizer_render(var$6, var$2, var$16, var$5, var$14, 1.0);
                if (var$1 !== null) {
                    var$11 = var$1.$argbPixels;
                    if (var$11 !== null) {
                        cw_WebClassifyMain_lastWidth = var$1.$width;
                        cw_WebClassifyMain_lastHeight = var$1.$height;
                        cw_WebClassifyMain_lastArgb = var$11;
                        var$1 = var$1.$ownershipGrid;
                        cw_WebClassifyMain_lastOwners = var$1.$clusterIndices0;
                        cw_WebClassifyMain_lastMembers = var$1.$memberIndices0;
                        cw_WebClassifyMain_computeHandles(cc_ScenePreparation$PreparedScene_clusters(var$9));
                        cw_WebClassifyMain_computeOverlays(cc_ScenePreparation$PreparedScene_clusters(var$9));
                        return;
                    }
                }
                var$1 = new jl_IllegalStateException;
                jl_Exception__init_(var$1, $rt_s(24));
                $rt_throw(var$1);
            }
            var$1 = ju_AbstractList$1_next(var$2);
            if (var$1 === null)
                break;
            var$15 = var$13 + 1 | 0;
            var$12[var$13] = var$1;
            var$13 = var$15;
        }
        var$1 = new jl_NullPointerException;
        jl_Exception__init_0(var$1);
        $rt_throw(var$1);
    }
    var$2 = new jl_IllegalArgumentException;
    jl_Exception__init_(var$2, $rt_s(25));
    $rt_throw(var$2);
},
cw_WebClassifyMain_clusterNameAt$jsocb$_28 = var$1 => {
    let var$2;
    cw_WebClassifyMain_$callClinit();
    var$2 = var$1;
    a: {
        if (var$2 >= 0) {
            var$1 = cw_WebClassifyMain_sceneSnapshot.$clusters0;
            if (var$2 < var$1.$size0) {
                var$1 = (ju_ArrayList_get(var$1, var$2)).$name3;
                break a;
            }
        }
        var$1 = $rt_s(4);
    }
    return $rt_ustr(var$1);
},
cw_WebClassifyMain_overlayCount$jsocb$_29 = () => {
    cw_WebClassifyMain_$callClinit();
    return cw_WebClassifyMain_overlayCount;
},
cw_WebClassifyMain_handleMembers$jsocb$_9 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap0(cw_WebClassifyMain_handleMemberIndices);
},
cw_WebClassifyMain_lastWidth$jsocb$_4 = () => {
    cw_WebClassifyMain_$callClinit();
    return cw_WebClassifyMain_lastWidth;
},
cw_WebClassifyMain_selectHandle$jsocb$_51 = var$1 => {
    let var$2, var$3;
    cw_WebClassifyMain_$callClinit();
    var$2 = var$1;
    if (var$2 >= 0 && var$2 < cw_WebClassifyMain_handleTotal) {
        var$3 = cw_WebClassifyMain_handleClusterIndices.data[var$2];
        cw_WebClassifyMain_selectedClusterIndex = var$3;
        cw_WebClassifyMain_selectedMemberIndex = cw_WebClassifyMain_handleMemberIndices.data[var$2];
        cw_WebClassifyMain_selectedHandleIndex = cw_WebClassifyMain_handleWithinMemberIndices.data[var$2];
        cw_WebClassifyMain_activeClusterIndex = var$3;
    }
},
cw_WebClassifyMain_endHandleDrag$jsocb$_43 = () => {
    cw_WebClassifyMain_$callClinit();
    cw_WebClassifyMain_endHandleDrag();
},
cw_WebClassifyMain_setSiteMemberKindName$jsocb$_50 = var$1 => {
    let var$2, $$je;
    cw_WebClassifyMain_$callClinit();
    var$1 = $rt_str(var$1);
    a: {
        b: {
            try {
                var$2 = cm_SiteMemberKind_valueOf(var$1);
                break b;
            } catch ($$e) {
                $$je = $rt_wrapException($$e);
                if ($$je instanceof jl_IllegalArgumentException) {
                } else {
                    throw $$e;
                }
            }
            var$2 = jl_StringBuilder__init_();
            jl_StringBuilder_append(jl_StringBuilder_append(var$2, $rt_s(26)), var$1);
            var$1 = jl_StringBuilder_toString(var$2);
            cw_WebClassifyMain_lastError = var$1;
            break a;
        }
        var$1 = cm_MetricMemberCompatibility_invalidNewMemberMessage(cw_WebClassifyMain_sceneSnapshot.$metricKind, var$2);
        if (ju_Optional_isPresent(var$1)) {
            var$1 = ju_Optional_get(var$1);
            cw_WebClassifyMain_lastError = var$1;
        } else {
            cw_WebClassifyMain_sceneSnapshot.$siteMemberKind = var$2;
            cw_WebClassifyMain_lastError = $rt_s(4);
            var$1 = $rt_s(4);
        }
    }
    return $rt_ustr(var$1);
},
cw_WebClassifyMain_overlayBy$jsocb$_36 = () => {
    cw_WebClassifyMain_$callClinit();
    return otji_JS_wrap(cw_WebClassifyMain_overlayBy);
},
cw_WebClassifyMain_handleTotal$jsocb$_15 = () => {
    cw_WebClassifyMain_$callClinit();
    return cw_WebClassifyMain_handleTotal;
},
cw_WebClassifyMain_clearSelection$jsocb$_53 = () => {
    cw_WebClassifyMain_$callClinit();
    cw_WebClassifyMain_clearSelection();
},
jl_ClassCastException = $rt_classWithoutFields(jl_RuntimeException),
otp_Platform = $rt_classWithoutFields(),
otp_Platform_clone = var$1 => {
    let copy = new var$1.constructor();
    for (let field in var$1) {
        if (var$1.hasOwnProperty(field)) {
            copy[field] = var$1[field];
        }
    }
    return copy;
},
otji_JS = $rt_classWithoutFields(),
otji_JS_wrap0 = var$1 => {
    let var$2, $result, $i, var$5;
    if (var$1 === null)
        return null;
    var$1 = var$1.data;
    var$2 = var$1.length;
    $result = new Int32Array(var$2);
    $i = 0;
    while ($i < var$2) {
        var$5 = var$1[$i];
        $result[$i] = var$5;
        $i = $i + 1 | 0;
    }
    return $result;
},
otji_JS_wrap = $array => {
    let var$2, $result, $i, var$5;
    if ($array === null)
        return null;
    $array = $array.data;
    var$2 = $array.length;
    $result = new Float64Array(var$2);
    $i = 0;
    while ($i < var$2) {
        var$5 = $array[$i];
        $result[$i] = var$5;
        $i = $i + 1 | 0;
    }
    return $result;
},
otci_IntegerUtil = $rt_classWithoutFields(),
ju_Comparator = $rt_classWithoutFields(0),
jl_String$_clinit_$lambda$_118_0 = $rt_classWithoutFields(),
jl_Character = $rt_classWithoutFields(),
jl_Character_TYPE = null,
jl_Character_characterCache = null,
jl_Character_$callClinit = () => {
    jl_Character_$callClinit = $rt_eraseClinit(jl_Character);
    jl_Character__clinit_();
},
jl_Character_forDigit = ($digit, $radix) => {
    jl_Character_$callClinit();
    if ($radix >= 2 && $radix <= 36 && $digit >= 0 && $digit < $radix)
        return $digit < 10 ? (48 + $digit | 0) & 65535 : ((97 + $digit | 0) - 10 | 0) & 65535;
    return 0;
},
jl_Character__clinit_ = () => {
    jl_Character_TYPE = $rt_cls($rt_charcls);
    jl_Character_characterCache = $rt_createArray(jl_Character, 128);
};
function cc_DiagramRasterizer() {
    let a = this; jl_Object.call(a);
    a.$pixels = null;
    a.$clusterIndices = null;
    a.$memberIndices = null;
    a.$sizeYp = 0;
    a.$sizeXp = 0;
}
let cc_DiagramRasterizer_render = ($this, $tFromPixels, $bImage, $classifier, $colorizer, $resolutionScale) => {
    let var$6, $y, var$8, var$9, var$10, $spec, var$12, var$13, var$14, var$15, var$16, var$17, var$18, var$19, var$20, var$21, var$22, var$23, var$24;
    $bImage = cg_Vector_xy($bImage.$qx - $bImage.$px, $bImage.$qy - $bImage.$py);
    $bImage = cg_Vector_xy(jl_Math_abs($bImage.$x0), jl_Math_abs($bImage.$y0));
    var$6 = $bImage.$x0 | 0;
    $y = $bImage.$y0 | 0;
    if (var$6 && $y) {
        $resolutionScale = jl_Math_min0(1.0, jl_Math_max0(4.9E-324, $resolutionScale));
        var$8 = var$6;
        var$9 = jl_Math_max(1, Long_lo((jl_Math_round(var$8 * $resolutionScale))));
        var$10 = $y;
        var$6 = jl_Math_max(1, Long_lo((jl_Math_round(var$10 * $resolutionScale))));
        if (!($this.$sizeYp >= var$6 && $this.$sizeXp >= var$9)) {
            $this.$sizeYp = var$6;
            $this.$sizeXp = var$9;
            $y = $rt_imul(var$6, var$9);
            $this.$pixels = $rt_createIntArray($y);
            $this.$clusterIndices = $rt_createIntArray($y);
            $this.$memberIndices = $rt_createIntArray($y);
        }
        $spec = new cc_DiagramRasterizer$GridSpec;
        $resolutionScale = var$8 / var$9;
        var$8 = var$10 / var$6;
        $spec.$sizeX = var$9;
        $spec.$sizeY = var$6;
        $spec.$sx = $resolutionScale;
        $spec.$sy = var$8;
    } else
        $spec = null;
    if ($spec === null)
        return null;
    $y = 0;
    while (true) {
        var$9 = $spec.$sizeY;
        if ($y >= var$9)
            break;
        var$12 = $spec.$sizeX;
        var$13 = $spec.$sx;
        var$14 = $spec.$sy;
        var$15 = 0;
        while (var$15 < var$12) {
            $bImage = cg_Vector_xy((var$15 + 0.5) * var$13, ($y + 0.5) * var$14);
            $resolutionScale = $tFromPixels.$mex;
            var$8 = $bImage.$x0;
            var$10 = $resolutionScale * var$8;
            var$16 = $tFromPixels.$mfx;
            $resolutionScale = $bImage.$y0;
            $bImage = cg_Vector_xy(var$10 + var$16 * $resolutionScale + $tFromPixels.$tx, $tFromPixels.$mey * var$8 + $tFromPixels.$mfy * $resolutionScale + $tFromPixels.$ty);
            var$17 = $classifier.$_0;
            cw_WebClassifyMain_$callClinit();
            var$18 = var$17.$ownershipSelector;
            var$19 = var$17.$clusters2;
            var$20 = var$17.$metric;
            var$6 = (-1);
            var$21 = (-1);
            var$8 = !var$18.$preferLowerScores ? (-Infinity) : Infinity;
            var$9 = 0;
            var$19 = var$19;
            while (var$9 < ju_TemplateCollections$ImmutableArrayList_size(var$19)) {
                var$17 = cm_ClusterMetric_evaluate(var$20, $bImage, ju_TemplateCollections$ImmutableArrayList_get(var$19, var$9));
                $resolutionScale = var$17.$score;
                var$22 = !var$18.$preferLowerScores ? (!($resolutionScale > var$8) ? 0 : 1) : !($resolutionScale < var$8) ? 0 : 1;
                a: {
                    if (!var$22) {
                        if ($resolutionScale !== var$8) {
                            $resolutionScale = var$8;
                            break a;
                        }
                        if (var$9 >= var$6) {
                            $resolutionScale = var$8;
                            break a;
                        }
                    }
                    var$21 = var$17.$memberIndex;
                    var$6 = var$9;
                }
                var$9 = var$9 + 1 | 0;
                var$8 = $resolutionScale;
            }
            var$9 = $rt_imul($y, var$12) + var$15 | 0;
            $this.$clusterIndices.data[var$9] = var$6;
            $this.$memberIndices.data[var$9] = var$21;
            if ($colorizer !== null) {
                var$23 = $this.$pixels;
                var$17 = $colorizer.$_00;
                if (var$6 < 0)
                    var$6 = cm_Rgba_toArgb(var$17.$background);
                else {
                    $bImage = ju_TemplateCollections$ImmutableArrayList_get(var$17.$clusters3, var$6);
                    var$16 = !var$17.$shadingEnabled ? 1.0 : 0.6 + 0.4 * jl_Math_exp( -var$8 / 480.0);
                    var$17 = $bImage.$color;
                    var$6 = cm_Rgba_toArgb(cm_Rgba__init_(cm_Rgba_clamp01(var$17.$r * var$16), cm_Rgba_clamp01(var$17.$g * var$16), cm_Rgba_clamp01(var$17.$b1 * var$16), var$17.$a2));
                }
                var$23.data[var$9] = var$6;
            }
            var$15 = var$15 + 1 | 0;
        }
        $y = $y + 1 | 0;
    }
    var$6 = $spec.$sizeX;
    $y = $colorizer === null ? 0 : 1;
    $tFromPixels = new cc_DiagramRasterizer$OwnershipGrid;
    var$23 = $this.$clusterIndices;
    var$24 = $this.$memberIndices;
    $tFromPixels.$width0 = var$6;
    $tFromPixels.$height0 = var$9;
    $tFromPixels.$clusterIndices0 = var$23;
    $tFromPixels.$memberIndices0 = var$24;
    $bImage = new cc_DiagramRasterizer$RasterResult;
    var$23 = $y ? $this.$pixels : null;
    $bImage.$width = var$6;
    $bImage.$height = var$9;
    $bImage.$argbPixels = var$23;
    $bImage.$ownershipGrid = $tFromPixels;
    return $bImage;
},
jl_Iterable = $rt_classWithoutFields(0),
ju_Collection = $rt_classWithoutFields(0),
ju_AbstractCollection = $rt_classWithoutFields(),
ju_AbstractCollection_isEmpty = $this => {
    return $this.$size() ? 0 : 1;
},
ju_SequencedCollection = $rt_classWithoutFields(0),
ju_List = $rt_classWithoutFields(0),
ju_List_of = ($e1, $e2, $e3, $e4) => {
    ju_Objects_requireNonNull($e1);
    ju_Objects_requireNonNull($e2);
    ju_Objects_requireNonNull($e3);
    ju_Objects_requireNonNull($e4);
    return ju_TemplateCollections$ImmutableArrayList__init_($rt_wrapArray(jl_Object, [$e1, $e2, $e3, $e4]));
};
function ju_AbstractList() {
    ju_AbstractCollection.call(this);
    this.$modCount = 0;
}
let ju_AbstractList_iterator = $this => {
    let var$1;
    var$1 = new ju_AbstractList$1;
    var$1.$this$0 = $this;
    var$1.$modCount0 = $this.$modCount;
    var$1.$size1 = $this.$size();
    var$1.$removeIndex = (-1);
    return var$1;
},
jl_Cloneable = $rt_classWithoutFields(0),
ju_RandomAccess = $rt_classWithoutFields(0);
function ju_ArrayList() {
    let a = this; ju_AbstractList.call(a);
    a.$array = null;
    a.$size0 = 0;
}
let ju_ArrayList__init_2 = $this => {
    ju_ArrayList__init_1($this, 10);
},
ju_ArrayList__init_ = () => {
    let var_0 = new ju_ArrayList();
    ju_ArrayList__init_2(var_0);
    return var_0;
},
ju_ArrayList__init_1 = ($this, $initialCapacity) => {
    let var$2;
    if ($initialCapacity >= 0) {
        $this.$array = $rt_createArray(jl_Object, $initialCapacity);
        return;
    }
    var$2 = new jl_IllegalArgumentException;
    jl_Exception__init_0(var$2);
    $rt_throw(var$2);
},
ju_ArrayList__init_0 = var_0 => {
    let var_1 = new ju_ArrayList();
    ju_ArrayList__init_1(var_1, var_0);
    return var_1;
},
ju_ArrayList_get = ($this, $index) => {
    ju_ArrayList_checkIndex($this, $index);
    return $this.$array.data[$index];
},
ju_ArrayList_size = $this => {
    return $this.$size0;
},
ju_ArrayList_set = ($this, $index, $element) => {
    let var$3, $old;
    ju_ArrayList_checkIndex($this, $index);
    var$3 = $this.$array.data;
    $old = var$3[$index];
    var$3[$index] = $element;
    return $old;
},
ju_ArrayList_add = ($this, $element) => {
    let var$2, var$3, var$4, var$5, var$6;
    var$2 = $this.$size0 + 1 | 0;
    var$3 = $this.$array.data.length;
    if (var$3 < var$2) {
        var$2 = var$3 >= 1073741823 ? 2147483647 : jl_Math_max(var$2, jl_Math_max(var$3 * 2 | 0, 5));
        var$4 = $this.$array;
        var$5 = var$4.data;
        var$4 = jlr_Array_newInstance(jl_Class_getComponentType(jl_Object_getClass(var$4)), var$2);
        var$3 = jl_Math_min(var$2, var$5.length);
        var$2 = 0;
        while (var$2 < var$3) {
            var$4.data[var$2] = var$5[var$2];
            var$2 = var$2 + 1 | 0;
        }
        $this.$array = var$4;
    }
    var$4 = $this.$array.data;
    var$6 = $this.$size0;
    $this.$size0 = var$6 + 1 | 0;
    var$4[var$6] = $element;
    $this.$modCount = $this.$modCount + 1 | 0;
    return 1;
},
ju_ArrayList_remove = ($this, $i) => {
    let var$2, $old, var$4, $i_0;
    ju_ArrayList_checkIndex($this, $i);
    var$2 = $this.$array.data;
    $old = var$2[$i];
    var$4 = $this.$size0 - 1 | 0;
    $this.$size0 = var$4;
    while ($i < var$4) {
        $i_0 = $i + 1 | 0;
        var$2[$i] = var$2[$i_0];
        $i = $i_0;
    }
    var$2[var$4] = null;
    $this.$modCount = $this.$modCount + 1 | 0;
    return $old;
},
ju_ArrayList_clear = $this => {
    let var$1, var$2, var$3, var$4, var$5, var$6;
    var$1 = $this.$array;
    var$2 = 0;
    var$3 = $this.$size0;
    var$4 = null;
    if (var$2 > var$3) {
        var$4 = new jl_IllegalArgumentException;
        jl_Exception__init_0(var$4);
        $rt_throw(var$4);
    }
    while (var$2 < var$3) {
        var$5 = var$1.data;
        var$6 = var$2 + 1 | 0;
        var$5[var$2] = var$4;
        var$2 = var$6;
    }
    $this.$size0 = 0;
    $this.$modCount = $this.$modCount + 1 | 0;
},
ju_ArrayList_checkIndex = ($this, $index) => {
    let var$2;
    if ($index >= 0 && $index < $this.$size0)
        return;
    var$2 = new jl_IndexOutOfBoundsException;
    jl_Exception__init_0(var$2);
    $rt_throw(var$2);
},
ju_Objects = $rt_classWithoutFields(),
ju_Objects_requireNonNull = $obj => {
    if ($obj !== null)
        return $obj;
    $obj = new jl_NullPointerException;
    jl_Exception__init_($obj, $rt_s(4));
    $rt_throw($obj);
};
function otji_JSWrapper() {
    jl_Object.call(this);
    this.$js = null;
}
let otji_JSWrapper_maybeUnwrap = $o => {
    if ($o === null)
        return null;
    return !($o instanceof $rt_objcls()) ? $o : !($o instanceof otji_JSWrapper) ? $o : $o.$js;
},
cm_DemoScenes = $rt_classWithoutFields(),
cm_DemoScenes_defaultClusters = () => {
    let var$1, var$2, var$3, var$4, var$5, var$6, var$7, var$8, var$9, var$10, var$11;
    var$1 = new cm_ClusterSite;
    var$2 = cm_Rgba__init_(1.0, 0.625, 0.25, 1.0);
    var$3 = cm_PointMember__init_(cg_Vector_xy((-154.5), 98.0));
    var$4 = cm_DemoScenes_circle((-51.5), (-279.0), 115.77672477661476);
    ju_Objects_requireNonNull(var$3);
    ju_Objects_requireNonNull(var$4);
    var$5 = new ju_TemplateCollections$TwoElementsList;
    var$5.$first = var$3;
    var$5.$second = var$4;
    cm_ClusterSite__init_(var$1, $rt_s(27), var$2, var$5);
    var$6 = cm_ClusterSite__init_0($rt_s(28), cm_Rgba__init_(0.2375, 0.59375, 0.95, 1.0), ju_List_of(cm_PointMember__init_(cg_Vector_xy((-61.5), (-40.0))), cm_PointMember__init_(cg_Vector_xy(343.5, (-133.0))), cm_PointMember__init_(cg_Vector_xy(372.5, 24.0)), cm_EllipseMember__init_(cg_Vector_xy(120.0, (-40.0)), cg_Vector_xy(285.5, 320.0), cg_Vector_xy(180.0, 50.0))));
    var$7 = new cm_ClusterSite;
    var$8 = cm_Rgba__init_(1.0, 0.3, 0.65, 1.0);
    var$9 = cm_SegmentMember__init_(cg_Vector_xy((-376.5), 12.0), cg_Vector_xy((-37.5), (-99.0)));
    var$3 = cm_SegmentMember__init_(cg_Vector_xy((-0.5), (-12.0)), cg_Vector_xy((-120.0), 220.0));
    var$4 = cm_SegmentMember__init_(cg_Vector_xy((-120.0), 220.0), cg_Vector_xy((-376.5), 12.0));
    var$5 = cm_SegmentMember__init_(cg_Vector_xy((-210.5), 87.0), cg_Vector_xy((-37.5), (-99.0)));
    var$10 = cm_SegmentMember__init_(cg_Vector_xy((-0.5), (-12.0)), cg_Vector_xy((-210.5), 87.0));
    var$11 = cm_PointMember__init_(cg_Vector_xy((-87.5), (-267.0)));
    ju_Objects_requireNonNull(var$9);
    ju_Objects_requireNonNull(var$3);
    ju_Objects_requireNonNull(var$4);
    ju_Objects_requireNonNull(var$5);
    ju_Objects_requireNonNull(var$10);
    ju_Objects_requireNonNull(var$11);
    cm_ClusterSite__init_(var$7, $rt_s(29), var$8, ju_TemplateCollections$ImmutableArrayList__init_($rt_wrapArray(jl_Object, [var$9, var$3, var$4, var$5, var$10, var$11])));
    var$2 = new cm_ClusterSite;
    var$4 = cm_Rgba__init_(0.375, 0.9, 0.27, 1.0);
    var$5 = cm_DemoScenes_circle(80.0, 200.0, 83.95981181493917);
    var$10 = cm_PointMember__init_(cg_Vector_xy(272.5, 107.0));
    var$11 = cm_LineMember__init_(cg_Vector_xy((-317.5), (-105.0)), cg_Vector_xy(261.5, (-182.0)));
    ju_Objects_requireNonNull(var$5);
    ju_Objects_requireNonNull(var$10);
    ju_Objects_requireNonNull(var$11);
    cm_ClusterSite__init_(var$2, $rt_s(30), var$4, ju_TemplateCollections$ImmutableArrayList__init_($rt_wrapArray(jl_Object, [var$5, var$10, var$11])));
    return ju_List_of(var$1, var$6, var$7, var$2);
},
cm_DemoScenes_circle = ($cx, $cy, $radius) => {
    let $center;
    $center = cg_Vector_xy($cx, $cy);
    return cm_CircleMember__init_($center, cg_Vector_add($center, cg_Vector_xy($radius, 0.0)));
},
jl_IndexOutOfBoundsException = $rt_classWithoutFields(jl_RuntimeException),
jl_IndexOutOfBoundsException__init_0 = $this => {
    jl_Exception__init_0($this);
},
jl_IndexOutOfBoundsException__init_2 = () => {
    let var_0 = new jl_IndexOutOfBoundsException();
    jl_IndexOutOfBoundsException__init_0(var_0);
    return var_0;
},
jl_IndexOutOfBoundsException__init_1 = ($this, $index) => {
    let var$2, var$3, var$4, var$5, var$6, var$7, var$8, var$9, var$10;
    var$2 = jl_StringBuilder__init_();
    var$3 = jl_StringBuilder_append(var$2, $rt_s(31));
    var$4 = var$3.$length;
    var$5 = 1;
    if ($index < 0) {
        var$5 = 0;
        $index =  -$index | 0;
    }
    a: {
        if ($rt_ucmp($index, 10) < 0) {
            if (var$5)
                jl_AbstractStringBuilder_insertSpace(var$3, var$4, var$4 + 1 | 0);
            else {
                jl_AbstractStringBuilder_insertSpace(var$3, var$4, var$4 + 2 | 0);
                var$6 = var$3.$buffer.data;
                var$7 = var$4 + 1 | 0;
                var$6[var$4] = 45;
                var$4 = var$7;
            }
            var$3.$buffer.data[var$4] = jl_Character_forDigit($index, 10);
        } else {
            var$8 = 1;
            var$7 = 1;
            var$9 = $rt_udiv((-1), 10);
            b: {
                while (true) {
                    var$10 = var$8 * 10 | 0;
                    if ($rt_ucmp(var$10, $index) > 0) {
                        var$10 = var$8;
                        break b;
                    }
                    var$7 = var$7 + 1 | 0;
                    if ($rt_ucmp(var$10, var$9) > 0)
                        break;
                    var$8 = var$10;
                }
            }
            if (!var$5)
                var$7 = var$7 + 1 | 0;
            jl_AbstractStringBuilder_insertSpace(var$3, var$4, var$4 + var$7 | 0);
            if (var$5)
                var$7 = var$4;
            else {
                var$6 = var$3.$buffer.data;
                var$7 = var$4 + 1 | 0;
                var$6[var$4] = 45;
            }
            while (true) {
                if (!var$10)
                    break a;
                var$6 = var$3.$buffer.data;
                var$9 = var$7 + 1 | 0;
                var$6[var$7] = jl_Character_forDigit($rt_udiv($index, var$10), 10);
                $index = $rt_umod($index, var$10);
                var$10 = $rt_udiv(var$10, 10);
                var$7 = var$9;
            }
        }
    }
    jl_Exception__init_($this, jl_StringBuilder_toString(var$2));
},
jl_IndexOutOfBoundsException__init_ = var_0 => {
    let var_1 = new jl_IndexOutOfBoundsException();
    jl_IndexOutOfBoundsException__init_1(var_1, var_0);
    return var_1;
};
function cm_SceneSnapshot() {
    let a = this; jl_Object.call(a);
    a.$name4 = null;
    a.$metricKind = null;
    a.$neighborOrder = null;
    a.$siteMemberKind = null;
    a.$nearestNeighborK = 0;
    a.$clusters0 = null;
}
let cm_SceneSnapshot__init_0 = $this => {
    cm_MetricKind_$callClinit();
    $this.$metricKind = cm_MetricKind_MINIMUM_DISTANCE;
    cm_NeighborOrder_$callClinit();
    $this.$neighborOrder = cm_NeighborOrder_NEAREST;
    cm_SiteMemberKind_$callClinit();
    $this.$siteMemberKind = cm_SiteMemberKind_POINT;
    $this.$nearestNeighborK = 1;
    $this.$clusters0 = ju_ArrayList__init_();
},
cm_SceneSnapshot__init_ = () => {
    let var_0 = new cm_SceneSnapshot();
    cm_SceneSnapshot__init_0(var_0);
    return var_0;
},
cm_SceneSnapshot_setNeighborOrder = ($this, $neighborOrder) => {
    $this.$neighborOrder = $neighborOrder;
},
cm_SceneSnapshot_setClusters = ($this, $sites) => {
    let var$2, var$3;
    ju_ArrayList_clear($this.$clusters0);
    var$2 = $this.$clusters0;
    var$3 = ju_AbstractList_iterator($sites);
    while (ju_AbstractList$1_hasNext(var$3)) {
        $sites = ju_AbstractList$1_next(var$3);
        if (!ju_ArrayList_add(var$2, $sites))
            continue;
    }
};
function jl_Enum() {
    let a = this; jl_Object.call(a);
    a.$name0 = null;
    a.$ordinal = 0;
}
let jl_Enum__init_ = ($this, $name, $ordinal) => {
    $this.$name0 = $name;
    $this.$ordinal = $ordinal;
},
jl_Enum_getDeclaringClass = $this => {
    let $result;
    $result = jl_Object_getClass($this);
    if (!(jl_Class_getSuperclass($result) !== $rt_cls(jl_Enum) ? 0 : 1))
        $result = jl_Class_getSuperclass($result);
    return $result;
},
jl_Enum_valueOf = ($enumType, $name) => {
    let $constants, var$4, var$5, var$6, $constant, var$8;
    if (!(!($enumType.$classInfo[$rt_meta].modifiers & 65536) ? 0 : 1))
        $constants = null;
    else {
        $enumType.$classInfo[$rt_meta].clinit();
        var$4 = $rt_enumConstants($enumType.$classInfo).length;
        $constants = otrr_ClassInfo_newArrayInstance($enumType.$classInfo, var$4);
        var$5 = 0;
        while (var$5 < var$4) {
            $constants.data[var$5] = $rt_enumConstants($enumType.$classInfo)[var$5];
            var$5 = var$5 + 1 | 0;
        }
    }
    $constants = $constants;
    if ($constants === null) {
        $enumType = new jl_IllegalArgumentException;
        jl_Exception__init_($enumType, $rt_s(32));
        $rt_throw($enumType);
    }
    $constants = $constants.data;
    var$4 = $constants.length;
    var$6 = 0;
    while (true) {
        if (var$6 >= var$4) {
            $constant = new jl_IllegalArgumentException;
            $enumType = jl_String_valueOf($enumType);
            var$8 = jl_String_valueOf($name);
            $name = jl_StringBuilder__init_();
            jl_StringBuilder_append(jl_StringBuilder_append(jl_StringBuilder_append(jl_StringBuilder_append(jl_StringBuilder_append($name, $rt_s(33)), $enumType), $rt_s(34)), var$8), $rt_s(35));
            jl_Exception__init_($constant, jl_StringBuilder_toString($name));
            $rt_throw($constant);
        }
        $constant = $constants[var$6];
        if (jl_String_equals($constant.$name0, $name))
            break;
        var$6 = var$6 + 1 | 0;
    }
    return $constant;
},
jl_Enum_compareTo = ($this, var$1) => {
    let var$2, var$3, var$4, var$5, var$6;
    var$1 = var$1;
    if (jl_Enum_getDeclaringClass(var$1) === jl_Enum_getDeclaringClass($this)) {
        var$2 = $this.$ordinal;
        var$3 = var$1.$ordinal;
        jl_Integer_$callClinit();
        return $rt_compare(var$2, var$3);
    }
    var$4 = new jl_IllegalArgumentException;
    var$5 = jl_String_valueOf(jl_Enum_getDeclaringClass($this));
    var$1 = jl_String_valueOf(jl_Enum_getDeclaringClass(var$1));
    var$6 = jl_StringBuilder__init_();
    jl_StringBuilder_append(jl_StringBuilder_append(jl_StringBuilder_append(jl_StringBuilder_append(var$6, $rt_s(36)), var$5), $rt_s(37)), var$1);
    jl_Exception__init_(var$4, jl_StringBuilder_toString(var$6));
    $rt_throw(var$4);
},
cm_MetricKind = $rt_classWithoutFields(jl_Enum),
cm_MetricKind_MINIMUM_DISTANCE = null,
cm_MetricKind_MAXIMUM_DISTANCE = null,
cm_MetricKind_SUM_OF_DISTANCES = null,
cm_MetricKind_MEAN_DISTANCE = null,
cm_MetricKind_KTH_NEAREST_DISTANCE = null,
cm_MetricKind_$VALUES = null,
cm_MetricKind_$callClinit = () => {
    cm_MetricKind_$callClinit = $rt_eraseClinit(cm_MetricKind);
    cm_MetricKind__clinit_();
},
cm_MetricKind_valueOf = $name => {
    cm_MetricKind_$callClinit();
    return jl_Enum_valueOf($rt_cls(cm_MetricKind), $name);
},
cm_MetricKind__init_0 = ($this, var$1, var$2) => {
    cm_MetricKind_$callClinit();
    jl_Enum__init_($this, var$1, var$2);
},
cm_MetricKind__init_ = (var_0, var_1) => {
    let var_2 = new cm_MetricKind();
    cm_MetricKind__init_0(var_2, var_0, var_1);
    return var_2;
},
cm_MetricKind__clinit_ = () => {
    let var$1, var$2, var$3;
    cm_MetricKind_MINIMUM_DISTANCE = cm_MetricKind__init_($rt_s(38), 0);
    cm_MetricKind_MAXIMUM_DISTANCE = cm_MetricKind__init_($rt_s(39), 1);
    cm_MetricKind_SUM_OF_DISTANCES = cm_MetricKind__init_($rt_s(40), 2);
    cm_MetricKind_MEAN_DISTANCE = cm_MetricKind__init_($rt_s(41), 3);
    var$1 = cm_MetricKind__init_($rt_s(42), 4);
    cm_MetricKind_KTH_NEAREST_DISTANCE = var$1;
    var$2 = $rt_createArray(cm_MetricKind, 5);
    var$3 = var$2.data;
    var$3[0] = cm_MetricKind_MINIMUM_DISTANCE;
    var$3[1] = cm_MetricKind_MAXIMUM_DISTANCE;
    var$3[2] = cm_MetricKind_SUM_OF_DISTANCES;
    var$3[3] = cm_MetricKind_MEAN_DISTANCE;
    var$3[4] = var$1;
    cm_MetricKind_$VALUES = var$2;
},
cm_NeighborOrder = $rt_classWithoutFields(jl_Enum),
cm_NeighborOrder_NEAREST = null,
cm_NeighborOrder_FARTHEST = null,
cm_NeighborOrder_$VALUES = null,
cm_NeighborOrder_$callClinit = () => {
    cm_NeighborOrder_$callClinit = $rt_eraseClinit(cm_NeighborOrder);
    cm_NeighborOrder__clinit_();
},
cm_NeighborOrder_valueOf = $name => {
    cm_NeighborOrder_$callClinit();
    return jl_Enum_valueOf($rt_cls(cm_NeighborOrder), $name);
},
cm_NeighborOrder__init_0 = ($this, var$1, var$2) => {
    cm_NeighborOrder_$callClinit();
    jl_Enum__init_($this, var$1, var$2);
},
cm_NeighborOrder__init_ = (var_0, var_1) => {
    let var_2 = new cm_NeighborOrder();
    cm_NeighborOrder__init_0(var_2, var_0, var_1);
    return var_2;
},
cm_NeighborOrder__clinit_ = () => {
    let var$1, var$2, var$3;
    cm_NeighborOrder_NEAREST = cm_NeighborOrder__init_($rt_s(43), 0);
    var$1 = cm_NeighborOrder__init_($rt_s(44), 1);
    cm_NeighborOrder_FARTHEST = var$1;
    var$2 = $rt_createArray(cm_NeighborOrder, 2);
    var$3 = var$2.data;
    var$3[0] = cm_NeighborOrder_NEAREST;
    var$3[1] = var$1;
    cm_NeighborOrder_$VALUES = var$2;
},
cm_SiteMemberKind = $rt_classWithoutFields(jl_Enum),
cm_SiteMemberKind_POINT = null,
cm_SiteMemberKind_LINE_SEGMENT = null,
cm_SiteMemberKind_CIRCLE = null,
cm_SiteMemberKind_ELLIPSE = null,
cm_SiteMemberKind_LINE = null,
cm_SiteMemberKind_$VALUES = null,
cm_SiteMemberKind_$callClinit = () => {
    cm_SiteMemberKind_$callClinit = $rt_eraseClinit(cm_SiteMemberKind);
    cm_SiteMemberKind__clinit_();
},
cm_SiteMemberKind_valueOf = $name => {
    cm_SiteMemberKind_$callClinit();
    return jl_Enum_valueOf($rt_cls(cm_SiteMemberKind), $name);
},
cm_SiteMemberKind__init_0 = ($this, var$1, var$2) => {
    cm_SiteMemberKind_$callClinit();
    jl_Enum__init_($this, var$1, var$2);
},
cm_SiteMemberKind__init_ = (var_0, var_1) => {
    let var_2 = new cm_SiteMemberKind();
    cm_SiteMemberKind__init_0(var_2, var_0, var_1);
    return var_2;
},
cm_SiteMemberKind__clinit_ = () => {
    let var$1, var$2, var$3;
    cm_SiteMemberKind_POINT = cm_SiteMemberKind__init_($rt_s(6), 0);
    cm_SiteMemberKind_LINE_SEGMENT = cm_SiteMemberKind__init_($rt_s(45), 1);
    cm_SiteMemberKind_CIRCLE = cm_SiteMemberKind__init_($rt_s(8), 2);
    cm_SiteMemberKind_ELLIPSE = cm_SiteMemberKind__init_($rt_s(10), 3);
    var$1 = cm_SiteMemberKind__init_($rt_s(9), 4);
    cm_SiteMemberKind_LINE = var$1;
    var$2 = $rt_createArray(cm_SiteMemberKind, 5);
    var$3 = var$2.data;
    var$3[0] = cm_SiteMemberKind_POINT;
    var$3[1] = cm_SiteMemberKind_LINE_SEGMENT;
    var$3[2] = cm_SiteMemberKind_CIRCLE;
    var$3[3] = cm_SiteMemberKind_ELLIPSE;
    var$3[4] = var$1;
    cm_SiteMemberKind_$VALUES = var$2;
},
jl_IllegalArgumentException = $rt_classWithoutFields(jl_RuntimeException),
otj_JSObject = $rt_classWithoutFields(0),
otjc_JSString = $rt_classWithoutFields(),
cm_MetricMemberCompatibility = $rt_classWithoutFields(),
cm_MetricMemberCompatibility_requiresPointOnlyMembers = $metricKind => {
    cm_MetricKind_$callClinit();
    return $metricKind !== cm_MetricKind_SUM_OF_DISTANCES && $metricKind !== cm_MetricKind_MEAN_DISTANCE && $metricKind !== cm_MetricKind_KTH_NEAREST_DISTANCE ? 0 : 1;
},
cm_MetricMemberCompatibility_pointOnlyMetricMessage = $metricKind => {
    let var$2;
    var$2 = $metricKind.$name0;
    $metricKind = jl_StringBuilder__init_();
    jl_StringBuilder_append(jl_StringBuilder_append($metricKind, var$2), $rt_s(46));
    return jl_StringBuilder_toString($metricKind);
},
cm_MetricMemberCompatibility_invalidMetricMessage = ($metricKind, $clusters) => {
    let var$3, $cluster;
    if (!cm_MetricMemberCompatibility_requiresPointOnlyMembers($metricKind))
        return ju_Optional_empty();
    var$3 = ju_AbstractList_iterator($clusters);
    while (ju_AbstractList$1_hasNext(var$3)) {
        $cluster = ju_AbstractList_iterator((ju_AbstractList$1_next(var$3)).$members);
        while (ju_AbstractList$1_hasNext($cluster)) {
            if (!(ju_AbstractList$1_next($cluster) instanceof cm_PointMember))
                return ju_Optional_of(cm_MetricMemberCompatibility_pointOnlyMetricMessage($metricKind));
        }
    }
    return ju_Optional_empty();
},
cm_MetricMemberCompatibility_invalidNewMemberMessage = ($metricKind, $memberKind) => {
    if (cm_MetricMemberCompatibility_requiresPointOnlyMembers($metricKind)) {
        cm_SiteMemberKind_$callClinit();
        if ($memberKind !== cm_SiteMemberKind_POINT)
            return ju_Optional_of(cm_MetricMemberCompatibility_pointOnlyMetricMessage($metricKind));
    }
    return ju_Optional_empty();
};
function ju_Optional() {
    jl_Object.call(this);
    this.$value0 = null;
}
let ju_Optional_emptyInstance = null,
ju_Optional__init_0 = ($this, $value) => {
    $this.$value0 = $value;
},
ju_Optional__init_ = var_0 => {
    let var_1 = new ju_Optional();
    ju_Optional__init_0(var_1, var_0);
    return var_1;
},
ju_Optional_empty = () => {
    if (ju_Optional_emptyInstance === null)
        ju_Optional_emptyInstance = ju_Optional__init_(null);
    return ju_Optional_emptyInstance;
},
ju_Optional_of = $value => {
    return ju_Optional__init_(ju_Objects_requireNonNull($value));
},
ju_Optional_get = $this => {
    let var$1;
    var$1 = $this.$value0;
    if (var$1 !== null)
        return var$1;
    var$1 = new ju_NoSuchElementException;
    jl_Exception__init_0(var$1);
    $rt_throw(var$1);
},
ju_Optional_isPresent = $this => {
    return $this.$value0 === null ? 0 : 1;
};
function cm_ClusterSite() {
    let a = this; jl_Object.call(a);
    a.$name3 = null;
    a.$color = null;
    a.$members = null;
}
let cm_ClusterSite__init_ = ($this, $name, $color, $members) => {
    let var$4, var$5, var$6;
    $this.$name3 = $name;
    $this.$color = $color;
    $name = ju_ArrayList__init_0($members.$size());
    $color = ju_AbstractList_iterator($members);
    var$4 = 0;
    while (true) {
        var$5 = $name.$array.data;
        var$6 = var$5.length;
        if (var$4 >= var$6)
            break;
        var$5[var$4] = ju_AbstractList$1_next($color);
        var$4 = var$4 + 1 | 0;
    }
    $name.$size0 = var$6;
    $this.$members = $name;
},
cm_ClusterSite__init_0 = (var_0, var_1, var_2) => {
    let var_3 = new cm_ClusterSite();
    cm_ClusterSite__init_(var_3, var_0, var_1, var_2);
    return var_3;
},
cm_ClusterSite_size = $this => {
    return $this.$members.$size0;
},
cm_ClusterSite_setMember = ($this, $index, $member) => {
    ju_ArrayList_set($this.$members, $index, $member);
},
cm_ClusterMember = $rt_classWithoutFields(0),
c_HandleVisibility = $rt_classWithoutFields(),
c_HandleVisibility_primaryHandleIndex = $member => {
    if ($member instanceof cm_CircleMember)
        return 1;
    if (!($member instanceof cm_EllipseMember))
        return 0;
    return 2;
},
c_HandleVisibility_isVisible = ($member, $handleIndex, $memberSelected) => {
    if (!($member instanceof cm_CircleMember) && !($member instanceof cm_EllipseMember) && !($member instanceof cm_LineMember))
        return 1;
    return $handleIndex != c_HandleVisibility_primaryHandleIndex($member) && !$memberSelected ? 0 : 1;
},
jl_Math = $rt_classWithoutFields(),
jl_Math_sin = var$1 => {
    return Math.sin(var$1);
},
jl_Math_cos = var$1 => {
    return Math.cos(var$1);
},
jl_Math_exp = var$1 => {
    return Math.exp(var$1);
},
jl_Math_sqrt = var$1 => {
    return Math.sqrt(var$1);
},
jl_Math_atan2 = (var$1, var$2) => {
    return Math.atan2(var$1, var$2);
},
jl_Math_round = var$1 => {
    return Long_fromNumber(var$1 + jl_Math_sign(var$1) * 0.5);
},
jl_Math_min = ($a, $b) => {
    if ($a < $b)
        $b = $a;
    return $b;
},
jl_Math_max = ($a, $b) => {
    if ($a > $b)
        $b = $a;
    return $b;
},
jl_Math_minImpl = (var$1, var$2) => {
    return Math.min(var$1, var$2);
},
jl_Math_min0 = (var$1, var$2) => {
    return jl_Math_minImpl(var$1, var$2);
},
jl_Math_maxImpl = (var$1, var$2) => {
    return Math.max(var$1, var$2);
},
jl_Math_max0 = (var$1, var$2) => {
    return jl_Math_maxImpl(var$1, var$2);
},
jl_Math_absImpl = var$1 => {
    return Math.abs(var$1);
},
jl_Math_abs = var$1 => {
    return jl_Math_absImpl(var$1);
},
jl_Math_sign = var$1 => {
    return Math.sign(var$1);
},
otjc_JSBoolean = $rt_classWithoutFields();
function cg_Vector() {
    let a = this; jl_Object.call(a);
    a.$x0 = 0.0;
    a.$y0 = 0.0;
}
let cg_Vector_ZERO = null,
cg_Vector_$callClinit = () => {
    cg_Vector_$callClinit = $rt_eraseClinit(cg_Vector);
    cg_Vector__clinit_();
},
cg_Vector_xy = (var$1, $y) => {
    let var$3;
    cg_Vector_$callClinit();
    var$3 = new cg_Vector;
    var$3.$x0 = var$1;
    var$3.$y0 = $y;
    return var$3;
},
cg_Vector_polar = ($r, $angleTurns) => {
    cg_Vector_$callClinit();
    $angleTurns = $angleTurns * 6.283185307179586;
    return cg_Vector_xy($r * jl_Math_cos($angleTurns), $r * jl_Math_sin($angleTurns));
},
cg_Vector_x = $this => {
    return $this.$x0;
},
cg_Vector_y = $this => {
    return $this.$y0;
},
cg_Vector_add = ($this, $o) => {
    return cg_Vector_xy($this.$x0 + $o.$x0, $this.$y0 + $o.$y0);
},
cg_Vector_sub = ($this, $o) => {
    return cg_Vector_xy($this.$x0 - $o.$x0, $this.$y0 - $o.$y0);
},
cg_Vector_mul = ($this, $k) => {
    return cg_Vector_xy($this.$x0 * $k, $this.$y0 * $k);
},
cg_Vector_lengthSquared = $this => {
    let var$1, var$2;
    var$1 = $this.$x0;
    var$1 = var$1 * var$1;
    var$2 = $this.$y0;
    return var$1 + var$2 * var$2;
},
cg_Vector_distanceTo = ($this, $o) => {
    return jl_Math_sqrt(cg_Vector_lengthSquared(cg_Vector_sub($this, $o)));
},
cg_Vector_dot = ($this, $o) => {
    return $this.$x0 * $o.$x0 + $this.$y0 * $o.$y0;
},
cg_Vector__clinit_ = () => {
    cg_Vector_ZERO = cg_Vector_xy(0.0, 0.0);
},
cm_SiteMemberFactory = $rt_classWithoutFields(),
cm_SiteMemberFactory_createDefault = ($kind, $clusterIndex, $memberIndex, $hint) => {
    let $half, $radius, $angle;
    a: {
        cm_SiteMemberFactory$1_$callClinit();
        switch (cm_SiteMemberFactory$1_$SwitchMap$cvdexplorer$model$SiteMemberKind.data[$kind.$ordinal]) {
            case 1:
                break;
            case 2:
                $half = cg_Vector_polar(22.0, 6.283185307179586 * ($memberIndex * 0.618033988749895 + $clusterIndex * 0.31));
                $kind = cm_SegmentMember__init_(cg_Vector_sub($hint, $half), cg_Vector_add($hint, $half));
                break a;
            case 3:
                $radius = cg_Vector_polar(22.0, 6.283185307179586 * ($memberIndex * 0.618033988749895 + $clusterIndex * 0.31));
                $kind = cm_CircleMember__init_($hint, cg_Vector_add($hint, $radius));
                break a;
            case 4:
                $angle = 6.283185307179586 * ($memberIndex * 0.618033988749895 + $clusterIndex * 0.31);
                $half = cg_Vector_polar(18.0, $angle);
                $kind = cm_EllipseMember__init_(cg_Vector_sub($hint, $half), cg_Vector_add($hint, $half), cg_Vector_add($hint, cg_Vector_polar(40.0, $angle + 1.5707963267948966)));
                break a;
            case 5:
                $half = cg_Vector_polar(22.0, 6.283185307179586 * ($memberIndex * 0.618033988749895 + $clusterIndex * 0.31));
                $kind = cm_LineMember__init_(cg_Vector_sub($hint, $half), cg_Vector_add($hint, $half));
                break a;
            default:
                $kind = new jl_MatchException;
                jl_Exception__init_1($kind, null, null);
                $rt_throw($kind);
        }
        $kind = cm_PointMember__init_($hint);
    }
    return $kind;
},
cw_SceneJsonJs = $rt_classWithoutFields(),
cw_SceneJsonJs_parse = $json => {
    let $root, $t, var$4, var$5, var$6, var$7, var$8, var$9, var$10, var$11, var$12, var$13, $$je;
    a: {
        try {
            $root = JSON.parse($rt_ustr($json));
            break a;
        } catch ($$e) {
            $$je = $rt_wrapException($$e);
            if ($$je instanceof jl_Throwable) {
                $t = $$je;
            } else {
                throw $$e;
            }
        }
        $json = new ci_SceneJsonException;
        jl_Exception__init_1($json, $rt_s(47), $t);
        $rt_throw($json);
    }
    if ($root === null) {
        $t = new ci_SceneJsonException;
        jl_Exception__init_($t, $rt_s(48));
        $rt_throw($t);
    }
    $json = new ci_SceneFileFormat$SceneFileV1;
    $json.$version = cw_SceneJsonJs_str($root, $rt_s(49));
    $json.$name1 = cw_SceneJsonJs_str($root, $rt_s(50));
    $json.$metricKind0 = cw_SceneJsonJs_str($root, $rt_s(51));
    $json.$neighborOrder0 = cw_SceneJsonJs_str($root, $rt_s(52));
    $json.$siteMemberKind0 = cw_SceneJsonJs_str($root, $rt_s(53));
    if (cw_SceneJsonJs_has$js_body$_6($root, "nearestNeighborK") ? 1 : 0) {
        var$4 = cw_SceneJsonJs_num($root, $rt_s(54)) | 0;
        jl_Integer_$callClinit();
        if (var$4 >= (-128) && var$4 <= 127) {
            b: {
                if (jl_Integer_integerCache === null) {
                    jl_Integer_integerCache = $rt_createArray(jl_Integer, 256);
                    var$5 = 0;
                    while (true) {
                        var$6 = jl_Integer_integerCache.data;
                        if (var$5 >= var$6.length)
                            break b;
                        var$6[var$5] = jl_Integer__init_(var$5 - 128 | 0);
                        var$5 = var$5 + 1 | 0;
                    }
                }
            }
            $t = jl_Integer_integerCache.data[var$4 + 128 | 0];
        } else
            $t = jl_Integer__init_(var$4);
        $json.$nearestNeighborK0 = $t;
    }
    var$7 = cw_SceneJsonJs_array$js_body$_8($root, "clusters");
    if (var$7 === null) {
        $json = new ci_SceneJsonException;
        jl_Exception__init_($json, $rt_s(55));
        $rt_throw($json);
    }
    $json.$clusters1 = ju_ArrayList__init_();
    var$4 = 0;
    while (var$4 < var$7.length) {
        $t = $json.$clusters1;
        $root = otji_JSWrapper_maybeUnwrap(var$7[var$4]);
        if ($root === null) {
            $json = new ci_SceneJsonException;
            jl_Exception__init_($json, $rt_s(56));
            $rt_throw($json);
        }
        var$8 = new ci_SceneFileFormat$ClusterJson;
        var$8.$name = cw_SceneJsonJs_str($root, $rt_s(50));
        var$9 = cw_SceneJsonJs_obj$js_body$_7($root, "color");
        if (var$9 !== null) {
            var$10 = new ci_SceneFileFormat$ColorJson;
            var$10.$r0 = cw_SceneJsonJs_num(var$9, $rt_s(57));
            var$10.$g0 = cw_SceneJsonJs_num(var$9, $rt_s(58));
            var$10.$b3 = cw_SceneJsonJs_num(var$9, $rt_s(59));
            var$10.$opacity = cw_SceneJsonJs_num(var$9, $rt_s(60));
            var$8.$color0 = var$10;
        }
        var$11 = cw_SceneJsonJs_array$js_body$_8($root, "members");
        if (var$11 === null) {
            $json = new ci_SceneJsonException;
            $t = var$8.$name;
            $root = jl_StringBuilder__init_();
            jl_StringBuilder_append(jl_StringBuilder_append($root, $rt_s(61)), $t);
            jl_Exception__init_($json, jl_StringBuilder_toString($root));
            $rt_throw($json);
        }
        var$10 = ju_ArrayList__init_();
        var$12 = 0;
        while (var$12 < var$11.length) {
            var$9 = otji_JSWrapper_maybeUnwrap(var$11[var$12]);
            var$13 = new ci_SceneFileFormat$MemberJson;
            if (var$9 !== null) {
                var$13.$kind = cw_SceneJsonJs_str(var$9, $rt_s(62));
                var$13.$x1 = cw_SceneJsonJs_optNum(var$9, $rt_s(63));
                var$13.$y1 = cw_SceneJsonJs_optNum(var$9, $rt_s(64));
                var$13.$ax = cw_SceneJsonJs_optNum(var$9, $rt_s(65));
                var$13.$ay = cw_SceneJsonJs_optNum(var$9, $rt_s(66));
                var$13.$bx = cw_SceneJsonJs_optNum(var$9, $rt_s(67));
                var$13.$by = cw_SceneJsonJs_optNum(var$9, $rt_s(68));
                var$13.$cx = cw_SceneJsonJs_optNum(var$9, $rt_s(69));
                var$13.$cy = cw_SceneJsonJs_optNum(var$9, $rt_s(70));
                var$13.$radius0 = cw_SceneJsonJs_optNum(var$9, $rt_s(71));
                var$13.$px0 = cw_SceneJsonJs_optNum(var$9, $rt_s(72));
                var$13.$py0 = cw_SceneJsonJs_optNum(var$9, $rt_s(73));
                var$13.$qx0 = cw_SceneJsonJs_optNum(var$9, $rt_s(74));
                var$13.$qy0 = cw_SceneJsonJs_optNum(var$9, $rt_s(75));
                var$13.$hx = cw_SceneJsonJs_optNum(var$9, $rt_s(76));
                var$13.$hy = cw_SceneJsonJs_optNum(var$9, $rt_s(77));
            }
            ju_ArrayList_add(var$10, var$13);
            var$12 = var$12 + 1 | 0;
        }
        var$8.$members0 = var$10;
        ju_ArrayList_add($t, var$8);
        var$4 = var$4 + 1 | 0;
    }
    return ci_SceneFileFormat_fromDto($json);
},
cw_SceneJsonJs_str = ($obj, $key) => {
    let $v;
    $v = cw_SceneJsonJs_strRaw$js_body$_9($obj, $rt_ustr($key));
    return $v === null ? null : $rt_str($v);
},
cw_SceneJsonJs_num = ($obj, $key) => {
    return cw_SceneJsonJs_numRaw$js_body$_10($obj, $rt_ustr($key));
},
cw_SceneJsonJs_optNum = ($obj, $key) => {
    let $v;
    if (!(cw_SceneJsonJs_has$js_body$_6($obj, $rt_ustr($key)) ? 1 : 0))
        return null;
    $v = cw_SceneJsonJs_numRaw$js_body$_10($obj, $rt_ustr($key));
    if (!(isNaN($v) ? 1 : 0))
        return jl_Double_valueOf($v);
    return null;
},
cw_SceneJsonJs_has$js_body$_6 = (var$1, var$2) => {
    return var$1 != null && Object.prototype.hasOwnProperty.call(var$1, var$2);
},
cw_SceneJsonJs_obj$js_body$_7 = (var$1, var$2) => {
    var v = var$1[var$2];
    return v == null ? null : v;
},
cw_SceneJsonJs_array$js_body$_8 = (var$1, var$2) => {
    var v = var$1[var$2];
    return Array.isArray(v) ? v : null;
},
cw_SceneJsonJs_strRaw$js_body$_9 = (var$1, var$2) => {
    var v = var$1[var$2];
    if (v == null) return null;
    return typeof v === 'string' ? v : String(v);
},
cw_SceneJsonJs_numRaw$js_body$_10 = (var$1, var$2) => {
    var v = var$1[var$2];
    if (v == null || typeof v !== 'number') return NaN;
    return v;
},
ci_SceneJsonException = $rt_classWithoutFields(jl_Exception),
ci_SceneJsonException__init_ = ($this, var$1) => {
    jl_Exception__init_($this, var$1);
},
ci_SceneJsonException__init_0 = var_0 => {
    let var_1 = new ci_SceneJsonException();
    ci_SceneJsonException__init_(var_1, var_0);
    return var_1;
},
cc_ScenePreparation = $rt_classWithoutFields(),
cc_ScenePreparation_MINIMUM_DISTANCE = null,
cc_ScenePreparation_MAXIMUM_DISTANCE = null,
cc_ScenePreparation_SUM_OF_DISTANCES = null,
cc_ScenePreparation_MEAN_DISTANCE = null,
cc_ScenePreparation_NEAREST_OWNERSHIP = null,
cc_ScenePreparation_FARTHEST_OWNERSHIP = null,
cc_ScenePreparation_$callClinit = () => {
    cc_ScenePreparation_$callClinit = $rt_eraseClinit(cc_ScenePreparation);
    cc_ScenePreparation__clinit_();
},
cc_ScenePreparation__clinit_ = () => {
    let var$1;
    cc_ScenePreparation_MINIMUM_DISTANCE = new cm_NearestMemberMetric;
    cc_ScenePreparation_MAXIMUM_DISTANCE = new cm_FarthestMemberMetric;
    cc_ScenePreparation_SUM_OF_DISTANCES = new cm_SumOfDistancesMetric;
    var$1 = new cm_MeanOfDistancesMetric;
    cm_MeanOfDistancesMetric_$callClinit();
    cc_ScenePreparation_MEAN_DISTANCE = var$1;
    cc_ScenePreparation_NEAREST_OWNERSHIP = cc_ClusterOwnershipSelector__init_(1);
    cc_ScenePreparation_FARTHEST_OWNERSHIP = cc_ClusterOwnershipSelector__init_(0);
};
function cr_ClusterColorizer() {
    let a = this; jl_Object.call(a);
    a.$clusters3 = null;
    a.$background = null;
    a.$shadingEnabled = 0;
}
let jl_Record = $rt_classWithoutFields();
function cc_ScenePreparation$PreparedScene() {
    let a = this; jl_Record.call(a);
    a.$clusters2 = null;
    a.$metric = null;
    a.$ownershipSelector = null;
}
let cc_ScenePreparation$PreparedScene_clusters = $this => {
    return $this.$clusters2;
};
function cm_Rgba() {
    let a = this; jl_Object.call(a);
    a.$r = 0.0;
    a.$g = 0.0;
    a.$b1 = 0.0;
    a.$a2 = 0.0;
}
let cm_Rgba_RED = null,
cm_Rgba_GREEN = null,
cm_Rgba_BLUE = null,
cm_Rgba_BLACK = null,
cm_Rgba_WHITE = null,
cm_Rgba_GRAY = null,
cm_Rgba_ORANGE = null,
cm_Rgba_CYAN = null,
cm_Rgba_$callClinit = () => {
    cm_Rgba_$callClinit = $rt_eraseClinit(cm_Rgba);
    cm_Rgba__clinit_();
},
cm_Rgba__init_0 = ($this, $r, $g, $b, $a) => {
    cm_Rgba_$callClinit();
    $this.$r = cm_Rgba_clamp01($r);
    $this.$g = cm_Rgba_clamp01($g);
    $this.$b1 = cm_Rgba_clamp01($b);
    $this.$a2 = cm_Rgba_clamp01($a);
},
cm_Rgba__init_ = (var_0, var_1, var_2, var_3) => {
    let var_4 = new cm_Rgba();
    cm_Rgba__init_0(var_4, var_0, var_1, var_2, var_3);
    return var_4;
},
cm_Rgba_rgb = ($r, $g, $b) => {
    cm_Rgba_$callClinit();
    return cm_Rgba__init_($r, $g, $b, 1.0);
},
cm_Rgba_gray = $value => {
    cm_Rgba_$callClinit();
    $value = cm_Rgba_clamp01($value);
    return cm_Rgba__init_($value, $value, $value, 1.0);
},
cm_Rgba_toArgb = $this => {
    return Long_lo((jl_Math_round($this.$a2 * 255.0))) << 24 | Long_lo((jl_Math_round($this.$r * 255.0))) << 16 | Long_lo((jl_Math_round($this.$g * 255.0))) << 8 | Long_lo((jl_Math_round($this.$b1 * 255.0)));
},
cm_Rgba_clamp01 = $v => {
    cm_Rgba_$callClinit();
    if ($v < 0.0)
        return 0.0;
    if (!($v > 1.0))
        return $v;
    return 1.0;
},
cm_Rgba__clinit_ = () => {
    cm_Rgba_RED = cm_Rgba_rgb(1.0, 0.0, 0.0);
    cm_Rgba_GREEN = cm_Rgba_rgb(0.0, 1.0, 0.0);
    cm_Rgba_BLUE = cm_Rgba_rgb(0.0, 0.0, 1.0);
    cm_Rgba_BLACK = cm_Rgba_rgb(0.0, 0.0, 0.0);
    cm_Rgba_WHITE = cm_Rgba_rgb(1.0, 1.0, 1.0);
    cm_Rgba_GRAY = cm_Rgba_gray(0.5);
    cm_Rgba_ORANGE = cm_Rgba_rgb(1.0, 0.647, 0.0);
    cm_Rgba_CYAN = cm_Rgba_rgb(0.0, 1.0, 1.0);
};
function cg_Box() {
    let a = this; jl_Object.call(a);
    a.$px = 0.0;
    a.$py = 0.0;
    a.$qx = 0.0;
    a.$qy = 0.0;
}
let cg_Box_pq = ($p, $q) => {
    let var$3, var$4, var$5, var$6, var$7;
    var$3 = new cg_Box;
    var$4 = $p.$x0;
    var$5 = $p.$y0;
    var$6 = $q.$x0;
    var$7 = $q.$y0;
    var$3.$px = var$4;
    var$3.$py = var$5;
    var$3.$qx = var$6;
    var$3.$qy = var$7;
    return var$3;
},
cc_DiagramRasterizer$Classifier = $rt_classWithoutFields(0);
function cw_WebClassifyMain$computeFrame$lambda$_2_0() {
    jl_Object.call(this);
    this.$_0 = null;
}
let cc_DiagramRasterizer$Colorizer = $rt_classWithoutFields(0);
function cw_WebClassifyMain$computeFrame$lambda$_2_1() {
    jl_Object.call(this);
    this.$_00 = null;
}
let jl_IllegalStateException = $rt_classWithoutFields(jl_RuntimeException);
function cc_DiagramRasterizer$RasterResult() {
    let a = this; jl_Record.call(a);
    a.$width = 0;
    a.$height = 0;
    a.$argbPixels = null;
    a.$ownershipGrid = null;
}
function cc_DiagramRasterizer$OwnershipGrid() {
    let a = this; jl_Record.call(a);
    a.$width0 = 0;
    a.$height0 = 0;
    a.$clusterIndices0 = null;
    a.$memberIndices0 = null;
}
function cm_PointMember() {
    jl_Record.call(this);
    this.$position = null;
}
let cm_PointMember__init_0 = ($this, $position) => {
    $this.$position = $position;
},
cm_PointMember__init_ = var_0 => {
    let var_1 = new cm_PointMember();
    cm_PointMember__init_0(var_1, var_0);
    return var_1;
},
cm_PointMember_distanceTo = ($this, $point) => {
    return cg_Vector_distanceTo($this.$position, $point);
},
cm_PointMember_handleCount = $this => {
    return 1;
},
cm_PointMember_getHandle = ($this, $index) => {
    if (!$index)
        return $this.$position;
    $rt_throw(jl_IndexOutOfBoundsException__init_($index));
},
cm_PointMember_withHandle = ($this, $index, $v) => {
    if (!$index)
        return cm_PointMember__init_($v);
    $rt_throw(jl_IndexOutOfBoundsException__init_($index));
};
function cm_EllipseMember() {
    let a = this; jl_Object.call(a);
    a.$focusA = null;
    a.$focusB = null;
    a.$controlHandle = null;
    a.$degenerate = 0;
    a.$c = 0.0;
    a.$a1 = 0.0;
    a.$b2 = 0.0;
    a.$centerX = 0.0;
    a.$centerY = 0.0;
    a.$cos0 = 0.0;
    a.$sin0 = 0.0;
}
let cm_EllipseMember__init_0 = ($this, $focusA, $focusB, $controlHandle) => {
    let $focusDist, $bSq, var$6, $focusHalf, $angle, var$9;
    $this.$focusA = $focusA;
    $this.$focusB = $focusB;
    $this.$controlHandle = $controlHandle;
    $this.$c = cg_Vector_distanceTo($focusA, $controlHandle) + cg_Vector_distanceTo($focusB, $controlHandle);
    $focusDist = cg_Vector_distanceTo($focusA, $focusB);
    $bSq = $this.$c;
    var$6 = !($bSq <= $focusDist + 1.0E-9) ? 0 : 1;
    $this.$degenerate = var$6;
    $bSq = 0.5 * $bSq;
    $this.$a1 = $bSq;
    $focusHalf = 0.5 * $focusDist;
    $bSq = $bSq * $bSq - $focusHalf * $focusHalf;
    $this.$b2 = !var$6 && $bSq > 0.0 ? jl_Math_sqrt($bSq) : NaN;
    $angle = $focusA.$x0;
    $focusDist = $focusB.$x0;
    $this.$centerX = 0.5 * ($angle + $focusDist);
    $focusHalf = $focusA.$y0;
    var$9 = $focusB.$y0;
    $this.$centerY = 0.5 * ($focusHalf + var$9);
    $angle = jl_Math_atan2(var$9 - $focusHalf, $focusDist - $angle);
    $this.$cos0 = jl_Math_cos($angle);
    $this.$sin0 = jl_Math_sin($angle);
},
cm_EllipseMember__init_ = (var_0, var_1, var_2) => {
    let var_3 = new cm_EllipseMember();
    cm_EllipseMember__init_0(var_3, var_0, var_1, var_2);
    return var_3;
},
cm_EllipseMember_distanceTo = ($this, $point) => {
    let var$2, var$3, $dx, $localX, $dy, var$7, $localY, var$9, var$10, var$11, var$12, var$13, var$14, var$15, var$16, var$17, var$18, var$19, var$20;
    if ($this.$degenerate) {
        var$2 = $this.$focusA;
        var$3 = $this.$focusB;
        $dx = $point.$x0;
        $localX = var$2.$x0;
        $dx = $dx - $localX;
        $dy = $point.$y0;
        var$7 = var$2.$y0;
        $dy = $dy - var$7;
        $localY = var$3.$x0 - $localX;
        $localX = var$3.$y0 - var$7;
        var$7 = $localY * $localY + $localX * $localX;
        if (var$7 <= 0.0)
            $dx = cg_Vector_distanceTo($point, var$2);
        else {
            $dx = jl_Math_max0(0.0, jl_Math_min0(1.0, ($dx * $localY + $dy * $localX) / var$7));
            $dy = cg_Vector_x(var$2) + $dx * $localY;
            $dx = cg_Vector_y(var$2) + $dx * $localX;
            $dy = cg_Vector_x($point) - $dy;
            $dx = cg_Vector_y($point) - $dx;
            $dx = jl_Math_sqrt($dy * $dy + $dx * $dx);
        }
        return $dx;
    }
    $dx = $point.$x0 - $this.$centerX;
    $dy = $point.$y0 - $this.$centerY;
    $localY = $this.$cos0;
    $localX = $dx * $localY;
    var$7 = $this.$sin0;
    $localX = $localX + $dy * var$7;
    $localY =  -$dx * var$7 + $dy * $localY;
    $dx = $this.$a1;
    $dy = $this.$b2;
    var$7 = jl_Math_abs($localX);
    var$9 = jl_Math_abs($localY);
    if (!($dx < $dy)) {
        var$10 = $dy;
        var$11 = var$9;
        $dy = $dx;
        $dx = var$10;
        var$9 = var$7;
        var$7 = var$11;
    }
    if ($dx <= 1.0E-12) {
        $dx = $localX - jl_Math_max0( -$dy, jl_Math_min0($dy, $localX));
        $dx = jl_Math_sqrt($dx * $dx + $localY * $localY);
    } else
        a: {
            if (!(var$7 > 1.0E-12)) {
                if (var$9 < $dy) {
                    $localY = $dy * $dy - $dx * $dx;
                    if ($localY > 1.0E-12) {
                        $localY = $dy * var$9 / $localY;
                        if ($localY < 1.0) {
                            $dx = $dx * jl_Math_sqrt(jl_Math_max0(0.0, 1.0 - $localY * $localY));
                            $dy = $dy * $localY - var$9;
                            $dx = jl_Math_sqrt($dy * $dy + $dx * $dx);
                            break a;
                        }
                    }
                }
                $dx = jl_Math_abs(var$9 - $dy);
            } else if (!(var$9 > 1.0E-12))
                $dx = jl_Math_abs(var$7 - $dx);
            else {
                $localY = var$9 * var$9 / ($dy * $dy) + var$7 * var$7 / ($dx * $dx) - 1.0;
                if (jl_Math_abs($localY) <= 1.0E-12)
                    $dx = 0.0;
                else {
                    $localX = $dy / $dx;
                    var$12 = $localX * $localX;
                    $dy = var$9 / $dy;
                    var$13 = var$7 / $dx;
                    var$14 = var$12 * $dy;
                    var$15 = var$13 - 1.0;
                    var$16 = $localY < 0.0 ? 0.0 : jl_Math_sqrt(var$14 * var$14 + var$13 * var$13) - 1.0;
                    if (!(var$15 > var$16)) {
                        $dx = var$16;
                        var$16 = var$15;
                        var$15 = $dx;
                    }
                    var$17 = 0.5 * (var$16 + var$15);
                    var$18 = 0;
                    $localY = (-2.0) * var$14 * var$14;
                    $localX = 2.0 * var$13 * var$13;
                    var$11 = var$17;
                    b: {
                        c: {
                            while (true) {
                                if (var$18 >= 12)
                                    break c;
                                var$10 = var$11 + var$12;
                                var$19 = var$11 + 1.0;
                                if (jl_Math_abs(var$10) < 1.0E-12)
                                    break c;
                                if (jl_Math_abs(var$19) < 1.0E-12)
                                    break c;
                                $dx = var$14 / var$10;
                                $dy = var$13 / var$19;
                                var$20 = $dx * $dx + $dy * $dy - 1.0;
                                if (jl_Math_abs(var$20) <= 1.0E-12)
                                    break;
                                $dx = $localY / (var$10 * var$10 * var$10) - $localX / (var$19 * var$19 * var$19);
                                if (jl_Math_abs($dx) < 1.0E-12)
                                    break c;
                                $dx = var$11 - var$20 / $dx;
                                if ($dx < var$16)
                                    break c;
                                if ($dx > var$15)
                                    break c;
                                if (isNaN($dx) ? 1 : 0)
                                    break c;
                                if (jl_Math_abs($dx - var$11) <= 1.0E-12 * (1.0 + jl_Math_abs($dx))) {
                                    var$11 = $dx;
                                    break b;
                                }
                                var$18 = var$18 + 1 | 0;
                                var$11 = $dx;
                            }
                            break b;
                        }
                        var$18 = 0;
                        d: {
                            while (true) {
                                if (var$18 >= 32) {
                                    var$11 = var$17;
                                    break d;
                                }
                                var$11 = 0.5 * (var$16 + var$15);
                                if (var$11 === var$16)
                                    break d;
                                if (var$11 === var$15)
                                    break d;
                                $dx = var$14 / (var$11 + var$12);
                                $dy = var$13 / (var$11 + 1.0);
                                $dx = $dx * $dx + $dy * $dy - 1.0;
                                if ($dx > 0.0)
                                    var$16 = var$11;
                                else {
                                    if (!($dx < 0.0))
                                        break;
                                    var$15 = var$11;
                                }
                                var$18 = var$18 + 1 | 0;
                                var$17 = var$11;
                            }
                        }
                    }
                    $dx = var$12 * var$9 / (var$11 + var$12);
                    $dy = var$7 / (var$11 + 1.0);
                    $dx = $dx - var$9;
                    $dy = $dy - var$7;
                    $dx = jl_Math_sqrt($dx * $dx + $dy * $dy);
                }
            }
        }
    return $dx;
},
cm_EllipseMember_handleCount = $this => {
    return 3;
},
cm_EllipseMember_getHandle = ($this, $index) => {
    let var$2;
    a: {
        switch ($index) {
            case 0:
                break;
            case 1:
                var$2 = $this.$focusB;
                break a;
            case 2:
                var$2 = $this.$controlHandle;
                break a;
            default:
                $rt_throw(jl_IndexOutOfBoundsException__init_($index));
        }
        var$2 = $this.$focusA;
    }
    return var$2;
},
cm_EllipseMember_withHandle = ($this, $index, $v) => {
    let var$3;
    a: {
        switch ($index) {
            case 0:
                var$3 = cm_EllipseMember__init_($v, $this.$focusB, $this.$controlHandle);
                break a;
            case 1:
                var$3 = cm_EllipseMember__init_($this.$focusA, $v, $this.$controlHandle);
                break a;
            case 2:
                var$3 = cm_EllipseMember__init_($this.$focusA, $this.$focusB, $v);
                break a;
            default:
        }
        $rt_throw(jl_IndexOutOfBoundsException__init_($index));
    }
    return var$3;
};
function cm_SegmentMember() {
    let a = this; jl_Record.call(a);
    a.$a0 = null;
    a.$b0 = null;
}
let cm_SegmentMember__init_0 = ($this, $a, $b) => {
    $this.$a0 = $a;
    $this.$b0 = $b;
},
cm_SegmentMember__init_ = (var_0, var_1) => {
    let var_2 = new cm_SegmentMember();
    cm_SegmentMember__init_0(var_2, var_0, var_1);
    return var_2;
},
cm_SegmentMember_distanceTo = ($this, $point) => {
    let $ap, $ab, $ab2, $t;
    $ap = cg_Vector_sub($point, $this.$a0);
    $ab = cg_Vector_sub($this.$b0, $this.$a0);
    $ab2 = cg_Vector_lengthSquared($ab);
    if ($ab2 <= 0.0)
        return cg_Vector_distanceTo($point, $this.$a0);
    $t = jl_Math_max0(0.0, jl_Math_min0(1.0, cg_Vector_dot($ap, $ab) / $ab2));
    return cg_Vector_distanceTo($point, cg_Vector_add($this.$a0, cg_Vector_mul($ab, $t)));
},
cm_SegmentMember_handleCount = $this => {
    return 2;
},
cm_SegmentMember_getHandle = ($this, $index) => {
    let var$2;
    a: {
        switch ($index) {
            case 0:
                break;
            case 1:
                var$2 = $this.$b0;
                break a;
            default:
                $rt_throw(jl_IndexOutOfBoundsException__init_($index));
        }
        var$2 = $this.$a0;
    }
    return var$2;
},
cm_SegmentMember_withHandle = ($this, $index, $v) => {
    let var$3;
    a: {
        switch ($index) {
            case 0:
                var$3 = cm_SegmentMember__init_($v, $this.$b0);
                break a;
            case 1:
                var$3 = cm_SegmentMember__init_($this.$a0, $v);
                break a;
            default:
        }
        $rt_throw(jl_IndexOutOfBoundsException__init_($index));
    }
    return var$3;
};
function cm_LineMember() {
    let a = this; jl_Record.call(a);
    a.$a = null;
    a.$b = null;
}
let cm_LineMember__init_0 = ($this, $a, $b) => {
    $this.$a = $a;
    $this.$b = $b;
},
cm_LineMember__init_ = (var_0, var_1) => {
    let var_2 = new cm_LineMember();
    cm_LineMember__init_0(var_2, var_0, var_1);
    return var_2;
},
cm_LineMember_distanceTo = ($this, $point) => {
    let $ap, $ab, $ab2, $t;
    $ap = cg_Vector_sub($point, $this.$a);
    $ab = cg_Vector_sub($this.$b, $this.$a);
    $ab2 = cg_Vector_lengthSquared($ab);
    if ($ab2 <= 0.0)
        return cg_Vector_distanceTo($point, $this.$a);
    $t = cg_Vector_dot($ap, $ab) / $ab2;
    return cg_Vector_distanceTo($point, cg_Vector_add($this.$a, cg_Vector_mul($ab, $t)));
},
cm_LineMember_handleCount = $this => {
    return 2;
},
cm_LineMember_getHandle = ($this, $index) => {
    let var$2;
    a: {
        switch ($index) {
            case 0:
                break;
            case 1:
                var$2 = $this.$b;
                break a;
            default:
                $rt_throw(jl_IndexOutOfBoundsException__init_($index));
        }
        var$2 = $this.$a;
    }
    return var$2;
},
cm_LineMember_withHandle = ($this, $index, $v) => {
    let var$3;
    a: {
        switch ($index) {
            case 0:
                var$3 = cm_LineMember__init_($v, $this.$b);
                break a;
            case 1:
                var$3 = cm_LineMember__init_($this.$a, $v);
                break a;
            default:
        }
        $rt_throw(jl_IndexOutOfBoundsException__init_($index));
    }
    return var$3;
},
ju_NoSuchElementException = $rt_classWithoutFields(jl_RuntimeException),
cm_ClusterNaming = $rt_classWithoutFields(),
cm_ClusterNaming_NAMES = null,
cm_ClusterNaming_HUE_CENTERS = null,
cm_ClusterNaming_$callClinit = () => {
    cm_ClusterNaming_$callClinit = $rt_eraseClinit(cm_ClusterNaming);
    cm_ClusterNaming__clinit_();
},
cm_ClusterNaming__clinit_ = () => {
    cm_ClusterNaming_NAMES = $rt_wrapArray(jl_String, [$rt_s(78), $rt_s(79), $rt_s(27), $rt_s(80), $rt_s(30), $rt_s(81), $rt_s(82), $rt_s(83), $rt_s(28), $rt_s(84), $rt_s(85), $rt_s(86), $rt_s(87), $rt_s(29)]);
    cm_ClusterNaming_HUE_CENTERS = $rt_createDoubleArrayFromData([0.0, 20.0, 35.0, 55.0, 100.0, 130.0, 160.0, 185.0, 210.0, 235.0, 255.0, 275.0, 300.0, 330.0]);
};
function cm_CircleMember() {
    let a = this; jl_Record.call(a);
    a.$center = null;
    a.$radiusHandle = null;
}
let cm_CircleMember__init_0 = ($this, $center, $radiusHandle) => {
    $this.$center = $center;
    $this.$radiusHandle = $radiusHandle;
},
cm_CircleMember__init_ = (var_0, var_1) => {
    let var_2 = new cm_CircleMember();
    cm_CircleMember__init_0(var_2, var_0, var_1);
    return var_2;
},
cm_CircleMember_radius = $this => {
    return cg_Vector_distanceTo($this.$center, $this.$radiusHandle);
},
cm_CircleMember_distanceTo = ($this, $point) => {
    return jl_Math_abs(cg_Vector_distanceTo($this.$center, $point) - cm_CircleMember_radius($this));
},
cm_CircleMember_handleCount = $this => {
    return 2;
},
cm_CircleMember_getHandle = ($this, $index) => {
    let var$2;
    a: {
        switch ($index) {
            case 0:
                break;
            case 1:
                var$2 = $this.$radiusHandle;
                break a;
            default:
                $rt_throw(jl_IndexOutOfBoundsException__init_($index));
        }
        var$2 = $this.$center;
    }
    return var$2;
},
cm_CircleMember_withHandle = ($this, $index, $v) => {
    let $offset, var$4;
    a: {
        switch ($index) {
            case 0:
                $offset = cg_Vector_sub($this.$radiusHandle, $this.$center);
                var$4 = cm_CircleMember__init_($v, cg_Vector_add($v, $offset));
                break a;
            case 1:
                var$4 = cm_CircleMember__init_($this.$center, $v);
                break a;
            default:
        }
        $rt_throw(jl_IndexOutOfBoundsException__init_($index));
    }
    return var$4;
},
cm_SiteMemberFactory$1 = $rt_classWithoutFields(),
cm_SiteMemberFactory$1_$SwitchMap$cvdexplorer$model$SiteMemberKind = null,
cm_SiteMemberFactory$1_$callClinit = () => {
    cm_SiteMemberFactory$1_$callClinit = $rt_eraseClinit(cm_SiteMemberFactory$1);
    cm_SiteMemberFactory$1__clinit_();
},
cm_SiteMemberFactory$1__clinit_ = () => {
    let var$1, var$2;
    cm_SiteMemberKind_$callClinit();
    var$1 = $rt_createIntArray((cm_SiteMemberKind_$VALUES.$clone0()).data.length);
    var$2 = var$1.data;
    cm_SiteMemberFactory$1_$SwitchMap$cvdexplorer$model$SiteMemberKind = var$1;
    var$2[cm_SiteMemberKind_POINT.$ordinal] = 1;
    var$2[cm_SiteMemberKind_LINE_SEGMENT.$ordinal] = 2;
    var$2[cm_SiteMemberKind_CIRCLE.$ordinal] = 3;
    var$2[cm_SiteMemberKind_ELLIPSE.$ordinal] = 4;
    var$2[cm_SiteMemberKind_LINE.$ordinal] = 5;
},
jl_MatchException = $rt_classWithoutFields(jl_RuntimeException),
ci_SceneFileFormat = $rt_classWithoutFields(),
ci_SceneFileFormat_fromDto = $dto => {
    let $siteMemberKind, $metricKind, $neighborOrder, $loaded, var$6, $snapshot, $invalidMetricMessage, $loadedNearestNeighborK, var$10, var$11, var$12, var$13, $cj, $color, $members, $$je;
    if ($dto === null) {
        $dto = new ci_SceneJsonException;
        jl_Exception__init_($dto, $rt_s(48));
        $rt_throw($dto);
    }
    $siteMemberKind = $dto.$version;
    if ($siteMemberKind !== null && jl_String_equals($siteMemberKind, $rt_s(88))) {
        $siteMemberKind = $dto.$clusters1;
        if ($siteMemberKind !== null && !ju_AbstractCollection_isEmpty($siteMemberKind)) {
            if ($dto.$clusters1.$size0 > 32) {
                $dto = new ci_SceneJsonException;
                jl_Exception__init_($dto, $rt_s(89));
                $rt_throw($dto);
            }
            $siteMemberKind = $dto.$metricKind0;
            if ($siteMemberKind !== null && !jl_String_isEmpty($siteMemberKind)) {
                if (jl_String_equals($rt_s(90), $siteMemberKind)) {
                    cm_MetricKind_$callClinit();
                    $metricKind = cm_MetricKind_MEAN_DISTANCE;
                } else
                    a: {
                        try {
                            $metricKind = cm_MetricKind_valueOf($siteMemberKind);
                            break a;
                        } catch ($$e) {
                            $$je = $rt_wrapException($$e);
                            if ($$je instanceof jl_IllegalArgumentException) {
                            } else {
                                throw $$e;
                            }
                        }
                        $dto = new ci_SceneJsonException;
                        $metricKind = jl_StringBuilder__init_();
                        jl_StringBuilder_append(jl_StringBuilder_append($metricKind, $rt_s(91)), $siteMemberKind);
                        jl_Exception__init_($dto, jl_StringBuilder_toString($metricKind));
                        $rt_throw($dto);
                    }
                $siteMemberKind = $dto.$neighborOrder0;
                if ($siteMemberKind !== null && !jl_String_isEmpty($siteMemberKind))
                    b: {
                        try {
                            $neighborOrder = cm_NeighborOrder_valueOf($siteMemberKind);
                            break b;
                        } catch ($$e) {
                            $$je = $rt_wrapException($$e);
                            if ($$je instanceof jl_IllegalArgumentException) {
                            } else {
                                throw $$e;
                            }
                        }
                        $dto = new ci_SceneJsonException;
                        $metricKind = jl_StringBuilder__init_();
                        jl_StringBuilder_append(jl_StringBuilder_append($metricKind, $rt_s(92)), $siteMemberKind);
                        jl_Exception__init_($dto, jl_StringBuilder_toString($metricKind));
                        $rt_throw($dto);
                    }
                else {
                    cm_NeighborOrder_$callClinit();
                    $neighborOrder = cm_NeighborOrder_NEAREST;
                }
                c: {
                    try {
                        $siteMemberKind = cm_SiteMemberKind_valueOf($dto.$siteMemberKind0);
                        break c;
                    } catch ($$e) {
                        $$je = $rt_wrapException($$e);
                        if ($$je instanceof jl_Exception) {
                        } else {
                            throw $$e;
                        }
                    }
                    $siteMemberKind = new ci_SceneJsonException;
                    $dto = $dto.$siteMemberKind0;
                    $metricKind = jl_StringBuilder__init_();
                    jl_StringBuilder_append(jl_StringBuilder_append($metricKind, $rt_s(93)), $dto);
                    jl_Exception__init_($siteMemberKind, jl_StringBuilder_toString($metricKind));
                    $rt_throw($siteMemberKind);
                }
                $loaded = ju_ArrayList__init_();
                var$6 = ju_AbstractList_iterator($dto.$clusters1);
                d: {
                    while (true) {
                        if (!ju_AbstractList$1_hasNext(var$6)) {
                            $snapshot = cm_MetricMemberCompatibility_invalidMetricMessage($metricKind, $loaded);
                            $invalidMetricMessage = null;
                            $snapshot = $snapshot.$value0;
                            if ($snapshot !== null)
                                $invalidMetricMessage = $snapshot;
                            $invalidMetricMessage = $invalidMetricMessage;
                            if ($invalidMetricMessage !== null) {
                                $dto = new ci_SceneJsonException;
                                jl_Exception__init_($dto, $invalidMetricMessage);
                                $rt_throw($dto);
                            }
                            $loadedNearestNeighborK = 1;
                            $invalidMetricMessage = $dto.$nearestNeighborK0;
                            if ($invalidMetricMessage !== null) {
                                $loadedNearestNeighborK = $invalidMetricMessage.$value3;
                                if (!($loadedNearestNeighborK >= 1 && $loadedNearestNeighborK <= 32)) {
                                    $dto = new ci_SceneJsonException;
                                    jl_Exception__init_($dto, $rt_s(94));
                                    $rt_throw($dto);
                                }
                            }
                            $snapshot = cm_SceneSnapshot__init_();
                            $invalidMetricMessage = $dto.$name1;
                            if ($invalidMetricMessage !== null && !jl_String_isBlank($invalidMetricMessage)) {
                                $dto = $dto.$name1;
                                var$10 = 0;
                                var$11 = $dto.$nativeString.length - 1 | 0;
                                e: {
                                    while (var$10 <= var$11) {
                                        if (jl_String_charAt($dto, var$10) > 32)
                                            break e;
                                        var$10 = var$10 + 1 | 0;
                                    }
                                }
                                while (var$10 <= var$11 && jl_String_charAt($dto, var$11) <= 32) {
                                    var$11 = var$11 + (-1) | 0;
                                }
                                var$12 = var$11 + 1 | 0;
                                var$11 = $dto.$nativeString.length;
                                var$13 = $rt_compare(var$10, var$12);
                                if (!var$13)
                                    $dto = jl_String_EMPTY;
                                else if (!(!var$10 && var$12 == var$11)) {
                                    if (var$10 >= 0 && var$13 <= 0 && var$12 <= var$11)
                                        $dto = jl_String__init_($dto.$nativeString.substring(var$10, var$12));
                                    else {
                                        $dto = new jl_StringIndexOutOfBoundsException;
                                        jl_Exception__init_0($dto);
                                        $rt_throw($dto);
                                    }
                                }
                                $snapshot.$name4 = $dto;
                            }
                            $snapshot.$metricKind = $metricKind;
                            $snapshot.$neighborOrder = $neighborOrder;
                            $snapshot.$siteMemberKind = $siteMemberKind;
                            $snapshot.$nearestNeighborK = $loadedNearestNeighborK;
                            cm_SceneSnapshot_setClusters($snapshot, $loaded);
                            return $snapshot;
                        }
                        $cj = ju_AbstractList$1_next(var$6);
                        $invalidMetricMessage = $cj.$name;
                        if ($invalidMetricMessage === null)
                            break;
                        $snapshot = $cj.$color0;
                        if ($snapshot === null) {
                            $dto = new ci_SceneJsonException;
                            $siteMemberKind = jl_StringBuilder__init_();
                            jl_StringBuilder_append(jl_StringBuilder_append($siteMemberKind, $rt_s(95)), $invalidMetricMessage);
                            jl_Exception__init_($dto, jl_StringBuilder_toString($siteMemberKind));
                            $rt_throw($dto);
                        }
                        $color = cm_Rgba__init_($snapshot.$r0, $snapshot.$g0, $snapshot.$b3, $snapshot.$opacity);
                        $invalidMetricMessage = $cj.$members0;
                        if ($invalidMetricMessage === null)
                            break d;
                        if (ju_AbstractCollection_isEmpty($invalidMetricMessage))
                            break d;
                        $members = ci_SceneFileFormat_parseMembers($cj.$members0, $cj.$name);
                        $invalidMetricMessage = cm_ClusterSite__init_0($cj.$name, $color, $members);
                        ju_ArrayList_add($loaded, $invalidMetricMessage);
                    }
                    $dto = new ci_SceneJsonException;
                    jl_Exception__init_($dto, $rt_s(96));
                    $rt_throw($dto);
                }
                $dto = new ci_SceneJsonException;
                $siteMemberKind = $cj.$name;
                $metricKind = jl_StringBuilder__init_();
                jl_StringBuilder_append(jl_StringBuilder_append($metricKind, $rt_s(61)), $siteMemberKind);
                jl_Exception__init_($dto, jl_StringBuilder_toString($metricKind));
                $rt_throw($dto);
            }
            $siteMemberKind = new ci_SceneJsonException;
            jl_Exception__init_($siteMemberKind, $rt_s(97));
            $rt_throw($siteMemberKind);
        }
        $dto = new ci_SceneJsonException;
        jl_Exception__init_($dto, $rt_s(55));
        $rt_throw($dto);
    }
    $siteMemberKind = new ci_SceneJsonException;
    jl_Exception__init_($siteMemberKind, $rt_s(98));
    $rt_throw($siteMemberKind);
},
ci_SceneFileFormat_parseMembers = ($members, $clusterName) => {
    let $out, var$4, $mj, var$6, var$7, $center, var$9;
    $out = ju_ArrayList__init_();
    var$4 = ju_AbstractList_iterator($members);
    a: {
        b: {
            c: {
                d: {
                    e: while (true) {
                        if (!ju_AbstractList$1_hasNext(var$4)) {
                            if ($out.$size0 <= 32)
                                return $out;
                            $members = new ci_SceneJsonException;
                            $out = jl_StringBuilder__init_();
                            jl_StringBuilder_append(jl_StringBuilder_append($out, $rt_s(99)), $clusterName);
                            ci_SceneJsonException__init_($members, jl_StringBuilder_toString($out));
                            $rt_throw($members);
                        }
                        $mj = ju_AbstractList$1_next(var$4);
                        $members = $mj.$kind;
                        if ($members === null) {
                            $members = new ci_SceneJsonException;
                            $out = jl_StringBuilder__init_();
                            jl_StringBuilder_append(jl_StringBuilder_append($out, $rt_s(100)), $clusterName);
                            jl_Exception__init_($members, jl_StringBuilder_toString($out));
                            $rt_throw($members);
                        }
                        f: {
                            var$6 = (-1);
                            if (!$members.$hashCode) {
                                var$7 = 0;
                                while (true) {
                                    if (var$7 >= $members.$nativeString.length)
                                        break f;
                                    $members.$hashCode = (31 * $members.$hashCode | 0) + $members.$nativeString.charCodeAt(var$7) | 0;
                                    var$7 = var$7 + 1 | 0;
                                }
                            }
                        }
                        g: {
                            switch ($members.$hashCode) {
                                case -938498114:
                                    if (!jl_String_equals($members, $rt_s(10)))
                                        break g;
                                    var$6 = 3;
                                    break g;
                                case 2336756:
                                    if (!jl_String_equals($members, $rt_s(9)))
                                        break g;
                                    var$6 = 4;
                                    break g;
                                case 76307824:
                                    if (!jl_String_equals($members, $rt_s(6)))
                                        break g;
                                    var$6 = 0;
                                    break g;
                                case 1338394056:
                                    if (!jl_String_equals($members, $rt_s(45)))
                                        break g;
                                    var$6 = 1;
                                    break g;
                                case 1988079824:
                                    if (!jl_String_equals($members, $rt_s(8)))
                                        break g;
                                    var$6 = 2;
                                    break g;
                                default:
                            }
                        }
                        h: {
                            i: {
                                j: {
                                    k: {
                                        l: {
                                            switch (var$6) {
                                                case 0:
                                                    break;
                                                case 1:
                                                    break l;
                                                case 2:
                                                    break j;
                                                case 3:
                                                    break i;
                                                case 4:
                                                    break k;
                                                default:
                                                    $members = new ci_SceneJsonException;
                                                    $out = $mj.$kind;
                                                    $mj = jl_StringBuilder__init_();
                                                    jl_StringBuilder_append(jl_StringBuilder_append(jl_StringBuilder_append(jl_StringBuilder_append($mj, $rt_s(101)), $out), $rt_s(102)), $clusterName);
                                                    jl_Exception__init_($members, jl_StringBuilder_toString($mj));
                                                    $rt_throw($members);
                                            }
                                            $members = $mj.$x1;
                                            if ($members === null)
                                                break e;
                                            if ($mj.$y1 === null)
                                                break e;
                                            $center = cm_PointMember__init_(cg_Vector_xy(jl_Double_doubleValue($members), jl_Double_doubleValue($mj.$y1)));
                                            ju_ArrayList_add($out, $center);
                                            break h;
                                        }
                                        $members = $mj.$ax;
                                        if ($members === null)
                                            break d;
                                        if ($mj.$ay === null)
                                            break d;
                                        if ($mj.$bx === null)
                                            break d;
                                        if ($mj.$by === null)
                                            break d;
                                        $center = cm_SegmentMember__init_(cg_Vector_xy(jl_Double_doubleValue($members), jl_Double_doubleValue($mj.$ay)), cg_Vector_xy(jl_Double_doubleValue($mj.$bx), jl_Double_doubleValue($mj.$by)));
                                        ju_ArrayList_add($out, $center);
                                        break h;
                                    }
                                    $members = $mj.$px0;
                                    if ($members === null)
                                        break a;
                                    $center = $mj.$py0;
                                    if ($center === null)
                                        break a;
                                    if ($mj.$qx0 === null)
                                        break a;
                                    if ($mj.$qy0 === null)
                                        break a;
                                    var$9 = cm_LineMember__init_(cg_Vector_xy($members.$value, $center.$value), cg_Vector_xy($mj.$qx0.$value, $mj.$qy0.$value));
                                    ju_ArrayList_add($out, var$9);
                                    break h;
                                }
                                $members = $mj.$cx;
                                if ($members === null)
                                    break b;
                                $center = $mj.$cy;
                                if ($center === null)
                                    break b;
                                var$9 = $mj.$radius0;
                                if (var$9 === null)
                                    break b;
                                if (var$9.$value < 0.0) {
                                    $members = new ci_SceneJsonException;
                                    $out = jl_StringBuilder__init_();
                                    jl_StringBuilder_append(jl_StringBuilder_append($out, $rt_s(103)), $clusterName);
                                    ci_SceneJsonException__init_($members, jl_StringBuilder_toString($out));
                                    $rt_throw($members);
                                }
                                $center = cg_Vector_xy($members.$value, $center.$value);
                                $members = cm_CircleMember__init_($center, cg_Vector_add($center, cg_Vector_xy(jl_Double_doubleValue($mj.$radius0), 0.0)));
                                ju_ArrayList_add($out, $members);
                                break h;
                            }
                            var$9 = $mj.$ax;
                            if (var$9 === null)
                                break c;
                            $center = $mj.$ay;
                            if ($center === null)
                                break c;
                            if ($mj.$bx === null)
                                break c;
                            if ($mj.$by === null)
                                break c;
                            if ($mj.$hx === null)
                                break c;
                            if ($mj.$hy === null)
                                break c;
                            $members = cm_EllipseMember__init_(cg_Vector_xy(var$9.$value, $center.$value), cg_Vector_xy($mj.$bx.$value, $mj.$by.$value), cg_Vector_xy($mj.$hx.$value, $mj.$hy.$value));
                            ju_ArrayList_add($out, $members);
                        }
                    }
                    $members = new ci_SceneJsonException;
                    $out = jl_StringBuilder__init_();
                    jl_StringBuilder_append(jl_StringBuilder_append($out, $rt_s(104)), $clusterName);
                    ci_SceneJsonException__init_($members, jl_StringBuilder_toString($out));
                    $rt_throw($members);
                }
                $members = new ci_SceneJsonException;
                $out = jl_StringBuilder__init_();
                jl_StringBuilder_append(jl_StringBuilder_append($out, $rt_s(105)), $clusterName);
                ci_SceneJsonException__init_($members, jl_StringBuilder_toString($out));
                $rt_throw($members);
            }
            $members = new ci_SceneJsonException;
            $out = jl_StringBuilder__init_();
            jl_StringBuilder_append(jl_StringBuilder_append($out, $rt_s(106)), $clusterName);
            jl_Exception__init_($members, jl_StringBuilder_toString($out));
            $rt_throw($members);
        }
        $members = new ci_SceneJsonException;
        $out = jl_StringBuilder__init_();
        jl_StringBuilder_append(jl_StringBuilder_append($out, $rt_s(107)), $clusterName);
        jl_Exception__init_($members, jl_StringBuilder_toString($out));
        $rt_throw($members);
    }
    $members = new ci_SceneJsonException;
    $out = jl_StringBuilder__init_();
    jl_StringBuilder_append(jl_StringBuilder_append($out, $rt_s(108)), $clusterName);
    jl_Exception__init_($members, jl_StringBuilder_toString($out));
    $rt_throw($members);
};
function cg_Transformation() {
    let a = this; jl_Object.call(a);
    a.$mex = 0.0;
    a.$mfx = 0.0;
    a.$tx = 0.0;
    a.$mey = 0.0;
    a.$mfy = 0.0;
    a.$ty = 0.0;
}
let cg_Transformation_IDENTITY = null,
cg_Transformation_$callClinit = () => {
    cg_Transformation_$callClinit = $rt_eraseClinit(cg_Transformation);
    cg_Transformation__clinit_();
},
cg_Transformation__init_0 = ($this, $mex, $mfx, $tx, $mey, $mfy, $ty) => {
    cg_Transformation_$callClinit();
    $this.$mex = $mex;
    $this.$mfx = $mfx;
    $this.$tx = $tx;
    $this.$mey = $mey;
    $this.$mfy = $mfy;
    $this.$ty = $ty;
},
cg_Transformation__init_ = (var_0, var_1, var_2, var_3, var_4, var_5) => {
    let var_6 = new cg_Transformation();
    cg_Transformation__init_0(var_6, var_0, var_1, var_2, var_3, var_4, var_5);
    return var_6;
},
cg_Transformation__clinit_ = () => {
    cg_Transformation_IDENTITY = cg_Transformation__init_(1.0, 0.0, 0.0, 0.0, 1.0, 0.0);
};
function cc_DiagramRasterizer$GridSpec() {
    let a = this; jl_Record.call(a);
    a.$sizeX = 0;
    a.$sizeY = 0;
    a.$sx = 0.0;
    a.$sy = 0.0;
}
function jl_Double() {
    jl_Number.call(this);
    this.$value = 0.0;
}
let jl_Double_TYPE = null,
jl_Double_$callClinit = () => {
    jl_Double_$callClinit = $rt_eraseClinit(jl_Double);
    jl_Double__clinit_();
},
jl_Double_doubleValue = $this => {
    return $this.$value;
},
jl_Double_valueOf = $d => {
    let var$2;
    jl_Double_$callClinit();
    var$2 = new jl_Double;
    var$2.$value = $d;
    return var$2;
},
jl_Double_compare = ($a, $b) => {
    let $diff, var$4, var$5;
    jl_Double_$callClinit();
    $diff = (!($a > $b) ? 0 : 1) - (!($b > $a) ? 0 : 1) | 0;
    if (!$diff) {
        var$4 = 1.0 / $a;
        var$5 = 1.0 / $b;
        $diff = (((!(var$4 > var$5) ? 0 : 1) - (!(var$5 > var$4) ? 0 : 1) | 0) + ($b !== $b ? 0 : 1) | 0) - ($a !== $a ? 0 : 1) | 0;
    }
    return $diff;
},
jl_Double_compareTo = ($this, var$1) => {
    return jl_Double_compare($this.$value, var$1.$value);
},
jl_Double__clinit_ = () => {
    jl_Double_TYPE = $rt_cls($rt_doublecls);
},
ju_TemplateCollections$AbstractImmutableList = $rt_classWithoutFields(ju_AbstractList);
function ju_TemplateCollections$TwoElementsList() {
    let a = this; ju_TemplateCollections$AbstractImmutableList.call(a);
    a.$first = null;
    a.$second = null;
}
let ju_TemplateCollections$TwoElementsList_size = $this => {
    return 2;
},
ju_TemplateCollections$TwoElementsList_get = ($this, $index) => {
    let var$2;
    if (!$index)
        return $this.$first;
    if ($index == 1)
        return $this.$second;
    var$2 = new jl_IndexOutOfBoundsException;
    jl_Exception__init_0(var$2);
    $rt_throw(var$2);
};
function ju_TemplateCollections$ImmutableArrayList() {
    ju_TemplateCollections$AbstractImmutableList.call(this);
    this.$list = null;
}
let ju_TemplateCollections$ImmutableArrayList__init_0 = ($this, $list) => {
    $this.$list = $list;
},
ju_TemplateCollections$ImmutableArrayList__init_ = var_0 => {
    let var_1 = new ju_TemplateCollections$ImmutableArrayList();
    ju_TemplateCollections$ImmutableArrayList__init_0(var_1, var_0);
    return var_1;
},
ju_TemplateCollections$ImmutableArrayList_get = ($this, $index) => {
    return $this.$list.data[$index];
},
ju_TemplateCollections$ImmutableArrayList_size = $this => {
    return $this.$list.data.length;
},
ju_Collections = $rt_classWithoutFields(),
ju_Collections_EMPTY_SET = null,
ju_Collections_EMPTY_MAP = null,
ju_Collections_EMPTY_LIST = null,
ju_Collections_EMPTY_ITERATOR = null,
ju_Collections_EMPTY_LIST_ITERATOR = null,
ju_Collections_reverseOrder = null,
ju_Collections_$callClinit = () => {
    ju_Collections_$callClinit = $rt_eraseClinit(ju_Collections);
    ju_Collections__clinit_();
},
ju_Collections__clinit_ = () => {
    ju_Collections_EMPTY_SET = new ju_Collections$1;
    ju_Collections_EMPTY_MAP = new ju_Collections$2;
    ju_Collections_EMPTY_LIST = new ju_Collections$3;
    ju_Collections_EMPTY_ITERATOR = new ju_Collections$4;
    ju_Collections_EMPTY_LIST_ITERATOR = new ju_Collections$5;
    ju_Collections_reverseOrder = new ju_Collections$_clinit_$lambda$_59_0;
};
function ci_SceneFileFormat$SceneFileV1() {
    let a = this; jl_Object.call(a);
    a.$version = null;
    a.$name1 = null;
    a.$metricKind0 = null;
    a.$neighborOrder0 = null;
    a.$siteMemberKind0 = null;
    a.$nearestNeighborK0 = null;
    a.$clusters1 = null;
}
function ci_SceneFileFormat$ClusterJson() {
    let a = this; jl_Object.call(a);
    a.$name = null;
    a.$color0 = null;
    a.$members0 = null;
}
function ci_SceneFileFormat$ColorJson() {
    let a = this; jl_Object.call(a);
    a.$r0 = 0.0;
    a.$g0 = 0.0;
    a.$b3 = 0.0;
    a.$opacity = 0.0;
}
let cm_ClusterMetric = $rt_classWithoutFields(0),
cm_ClusterMetric_evaluate = ($this, $point, $cluster) => {
    return $this.$evaluate0($point, $cluster.$members);
},
cm_NearestMemberMetric = $rt_classWithoutFields(),
cm_NearestMemberMetric_evaluate = ($this, $point, $members) => {
    let $best, $bestIndex, $i, $d;
    $best = Infinity;
    $bestIndex = (-1);
    $i = 0;
    $members = $members;
    while ($i < $members.$size0) {
        $d = (ju_ArrayList_get($members, $i)).$distanceTo($point);
        if ($d < $best) {
            $bestIndex = $i;
            $best = $d;
        }
        $i = $i + 1 | 0;
    }
    return cm_ClusterMetric$Result__init_($best, $bestIndex);
},
cm_FarthestMemberMetric = $rt_classWithoutFields(),
cm_FarthestMemberMetric_evaluate = ($this, $point, $members) => {
    let $best, $bestIndex, $i, $d;
    if (ju_AbstractCollection_isEmpty($members))
        return cm_ClusterMetric$Result__init_(Infinity, (-1));
    $best = (-Infinity);
    $bestIndex = (-1);
    $i = 0;
    $members = $members;
    while ($i < $members.$size0) {
        $d = (ju_ArrayList_get($members, $i)).$distanceTo($point);
        if ($d > $best) {
            $bestIndex = $i;
            $best = $d;
        }
        $i = $i + 1 | 0;
    }
    return cm_ClusterMetric$Result__init_($best, $bestIndex);
},
cm_SumOfDistancesMetric = $rt_classWithoutFields(),
cm_SumOfDistancesMetric_evaluate = ($this, $point, $members) => {
    let $sum, var$4;
    if (ju_AbstractCollection_isEmpty($members))
        return cm_ClusterMetric$Result__init_(Infinity, (-1));
    $sum = 0.0;
    var$4 = ju_AbstractList_iterator($members);
    while (ju_AbstractList$1_hasNext(var$4)) {
        $sum = $sum + (ju_AbstractList$1_next(var$4)).$distanceTo($point);
    }
    return cm_ClusterMetric$Result__init_($sum, (-1));
},
cm_MeanOfDistancesMetric = $rt_classWithoutFields(),
cm_MeanOfDistancesMetric_SUM = null,
cm_MeanOfDistancesMetric_$callClinit = () => {
    cm_MeanOfDistancesMetric_$callClinit = $rt_eraseClinit(cm_MeanOfDistancesMetric);
    cm_MeanOfDistancesMetric__clinit_();
},
cm_MeanOfDistancesMetric_evaluate = ($this, $point, $members) => {
    let var$3;
    if (ju_AbstractCollection_isEmpty($members))
        return cm_ClusterMetric$Result__init_(Infinity, (-1));
    var$3 = new cm_ClusterMetric$Result;
    cm_MeanOfDistancesMetric_$callClinit();
    cm_ClusterMetric$Result__init_0(var$3, (cm_SumOfDistancesMetric_evaluate(cm_MeanOfDistancesMetric_SUM, $point, $members)).$score / $members.$size0, (-1));
    return var$3;
},
cm_MeanOfDistancesMetric__clinit_ = () => {
    cm_MeanOfDistancesMetric_SUM = new cm_SumOfDistancesMetric;
};
function cc_ClusterOwnershipSelector() {
    jl_Object.call(this);
    this.$preferLowerScores = 0;
}
let cc_ClusterOwnershipSelector__init_0 = ($this, $preferLowerScores) => {
    $this.$preferLowerScores = $preferLowerScores;
},
cc_ClusterOwnershipSelector__init_ = var_0 => {
    let var_1 = new cc_ClusterOwnershipSelector();
    cc_ClusterOwnershipSelector__init_0(var_1, var_0);
    return var_1;
},
cc_ScenePreparation$1 = $rt_classWithoutFields(),
cc_ScenePreparation$1_$SwitchMap$cvdexplorer$metric$MetricKind = null,
cc_ScenePreparation$1_$SwitchMap$cvdexplorer$model$NeighborOrder = null,
cc_ScenePreparation$1_$callClinit = () => {
    cc_ScenePreparation$1_$callClinit = $rt_eraseClinit(cc_ScenePreparation$1);
    cc_ScenePreparation$1__clinit_();
},
cc_ScenePreparation$1__clinit_ = () => {
    let var$1, var$2;
    cm_NeighborOrder_$callClinit();
    var$1 = $rt_createIntArray((cm_NeighborOrder_$VALUES.$clone0()).data.length);
    var$2 = var$1.data;
    cc_ScenePreparation$1_$SwitchMap$cvdexplorer$model$NeighborOrder = var$1;
    var$2[cm_NeighborOrder_NEAREST.$ordinal] = 1;
    var$2[cm_NeighborOrder_FARTHEST.$ordinal] = 2;
    cm_MetricKind_$callClinit();
    var$2 = $rt_createIntArray((cm_MetricKind_$VALUES.$clone0()).data.length);
    var$1 = var$2.data;
    cc_ScenePreparation$1_$SwitchMap$cvdexplorer$metric$MetricKind = var$2;
    var$1[cm_MetricKind_MINIMUM_DISTANCE.$ordinal] = 1;
    var$1[cm_MetricKind_MAXIMUM_DISTANCE.$ordinal] = 2;
    var$1[cm_MetricKind_SUM_OF_DISTANCES.$ordinal] = 3;
    var$1[cm_MetricKind_MEAN_DISTANCE.$ordinal] = 4;
    var$1[cm_MetricKind_KTH_NEAREST_DISTANCE.$ordinal] = 5;
},
juf_ToIntFunction = $rt_classWithoutFields(0),
cc_ScenePreparation$metricFor$lambda$_2_0 = $rt_classWithoutFields();
function cm_KthNearestPointDistanceMetric() {
    jl_Object.call(this);
    this.$k = 0;
}
let cm_KthNearestPointDistanceMetric_evaluate = ($this, $point, $members) => {
    let $kth, var$4, $distances, $i, $member, var$8, var$9, var$10, var$11, var$12, var$13, var$14, var$15, var$16, var$17, var$18, var$19, var$20, var$21, $pm;
    if (!ju_AbstractCollection_isEmpty($members)) {
        $kth = $members;
        var$4 = $kth.$size0;
        if (var$4 >= $this.$k) {
            $distances = ju_ArrayList__init_0(var$4);
            $i = 0;
            while (true) {
                if ($i >= $kth.$size0) {
                    ju_Collections_$callClinit();
                    $point = ju_Comparator$NaturalOrder_instance();
                    if ($point === null)
                        $point = ju_Comparator$NaturalOrder_instance();
                    $member = $distances;
                    var$8 = $rt_createArray(jl_Object, $member.$size0);
                    var$9 = var$8.data;
                    $distances = $distances;
                    var$4 = $distances.$size0;
                    var$10 = var$9.length;
                    if (var$10 < var$4)
                        var$11 = jlr_Array_newInstance(jl_Class_getComponentType(jl_Object_getClass(var$8)), var$4);
                    else {
                        while (var$4 < var$10) {
                            var$9[var$4] = null;
                            var$4 = var$4 + 1 | 0;
                        }
                        var$11 = var$8;
                    }
                    $i = 0;
                    $distances = ju_AbstractList_iterator($distances);
                    while (ju_AbstractList$1_hasNext($distances)) {
                        var$12 = var$11.data;
                        var$4 = $i + 1 | 0;
                        var$12[$i] = ju_AbstractList$1_next($distances);
                        $i = var$4;
                    }
                    if (var$10) {
                        if ($point === null)
                            ju_Comparator$NaturalOrder_instance();
                        var$12 = $rt_createArray(jl_Object, var$10);
                        var$13 = 1;
                        var$14 = var$8;
                        while (var$13 < var$10) {
                            var$15 = 0;
                            while (true) {
                                var$16 = var$14.data;
                                var$4 = var$16.length;
                                if (var$15 >= var$4)
                                    break;
                                var$17 = jl_Math_min(var$4, var$15 + var$13 | 0);
                                var$18 = var$15 + (2 * var$13 | 0) | 0;
                                var$19 = jl_Math_min(var$4, var$18);
                                var$20 = var$15;
                                var$21 = var$17;
                                a: {
                                    b: {
                                        while (var$15 != var$17) {
                                            if (var$21 == var$19)
                                                break b;
                                            $point = var$16[var$15];
                                            $members = var$16[var$21];
                                            if ($point.$compareTo($members) > 0) {
                                                var$11 = var$12.data;
                                                $i = var$20 + 1 | 0;
                                                var$11[var$20] = $members;
                                                var$21 = var$21 + 1 | 0;
                                            } else {
                                                var$11 = var$12.data;
                                                $i = var$20 + 1 | 0;
                                                var$11[var$20] = $point;
                                                var$15 = var$15 + 1 | 0;
                                            }
                                            var$20 = $i;
                                        }
                                        while (true) {
                                            if (var$21 >= var$19)
                                                break a;
                                            var$11 = var$12.data;
                                            $i = var$20 + 1 | 0;
                                            var$4 = var$21 + 1 | 0;
                                            var$11[var$20] = var$16[var$21];
                                            var$20 = $i;
                                            var$21 = var$4;
                                        }
                                    }
                                    while (true) {
                                        if (var$15 >= var$17)
                                            break a;
                                        var$11 = var$12.data;
                                        $i = var$20 + 1 | 0;
                                        var$4 = var$15 + 1 | 0;
                                        var$11[var$20] = var$16[var$15];
                                        var$20 = $i;
                                        var$15 = var$4;
                                    }
                                }
                                var$15 = var$18;
                            }
                            var$13 = var$13 * 2 | 0;
                            var$11 = var$14;
                            var$14 = var$12;
                            var$12 = var$11;
                        }
                        c: {
                            if (var$14 !== var$8) {
                                $i = 0;
                                while (true) {
                                    var$8 = var$14.data;
                                    if ($i >= var$8.length)
                                        break c;
                                    var$12.data[$i] = var$8[$i];
                                    $i = $i + 1 | 0;
                                }
                            }
                        }
                    }
                    $i = 0;
                    while ($i < var$10) {
                        ju_ArrayList_set($member, $i, var$9[$i]);
                        $i = $i + 1 | 0;
                    }
                    $kth = ju_ArrayList_get($member, $this.$k - 1 | 0);
                    return cm_ClusterMetric$Result__init_($kth.$distance, $kth.$index0);
                }
                $member = ju_ArrayList_get($kth, $i);
                if (!($member instanceof cm_PointMember))
                    break;
                $pm = $member;
                $members = new cm_KthNearestPointDistanceMetric$1DistanceWithIndex;
                $members.$distance = cg_Vector_distanceTo($pm.$position, $point);
                $members.$index0 = $i;
                ju_ArrayList_add($distances, $members);
                $i = $i + 1 | 0;
            }
            return cm_ClusterMetric$Result__init_(Infinity, (-1));
        }
    }
    return cm_ClusterMetric$Result__init_(Infinity, (-1));
},
jl_NullPointerException = $rt_classWithoutFields(jl_RuntimeException);
function cc_DiagramRasterizer$Classification() {
    let a = this; jl_Record.call(a);
    a.$clusterIndex = 0;
    a.$score1 = 0.0;
    a.$memberIndex0 = 0;
}
function ju_TemplateCollections$SingleElementList() {
    ju_TemplateCollections$AbstractImmutableList.call(this);
    this.$value1 = null;
}
let ju_TemplateCollections$SingleElementList_size = $this => {
    return 1;
},
ju_TemplateCollections$SingleElementList_get = ($this, $index) => {
    let var$2;
    if (!$index)
        return $this.$value1;
    var$2 = new jl_IndexOutOfBoundsException;
    jl_Exception__init_0(var$2);
    $rt_throw(var$2);
};
function ci_SceneFileFormat$MemberJson() {
    let a = this; jl_Object.call(a);
    a.$kind = null;
    a.$x1 = null;
    a.$y1 = null;
    a.$ax = null;
    a.$ay = null;
    a.$bx = null;
    a.$by = null;
    a.$cx = null;
    a.$cy = null;
    a.$radius0 = null;
    a.$px0 = null;
    a.$py0 = null;
    a.$qx0 = null;
    a.$qy0 = null;
    a.$hx = null;
    a.$hy = null;
}
function ju_OptionalInt() {
    jl_Object.call(this);
    this.$value2 = 0;
}
let ju_OptionalInt_emptyInstance = null,
ju_OptionalInt__init_0 = ($this, $value) => {
    $this.$value2 = $value;
},
ju_OptionalInt__init_ = var_0 => {
    let var_1 = new ju_OptionalInt();
    ju_OptionalInt__init_0(var_1, var_0);
    return var_1;
},
ju_Set = $rt_classWithoutFields(0),
ju_AbstractSet = $rt_classWithoutFields(ju_AbstractCollection),
ju_TemplateCollections$AbstractImmutableSet = $rt_classWithoutFields(ju_AbstractSet),
ju_Collections$1 = $rt_classWithoutFields(ju_TemplateCollections$AbstractImmutableSet),
ju_Map = $rt_classWithoutFields(0),
ju_AbstractMap = $rt_classWithoutFields(),
ju_TemplateCollections$AbstractImmutableMap = $rt_classWithoutFields(ju_AbstractMap),
ju_Collections$2 = $rt_classWithoutFields(ju_TemplateCollections$AbstractImmutableMap),
ju_Collections$3 = $rt_classWithoutFields(ju_TemplateCollections$AbstractImmutableList),
ju_Collections$3_get = ($this, $index) => {
    let var$2;
    var$2 = new jl_IndexOutOfBoundsException;
    jl_Exception__init_0(var$2);
    $rt_throw(var$2);
},
ju_Collections$3_size = $this => {
    return 0;
},
ju_Collections$3_iterator = $this => {
    ju_Collections_$callClinit();
    return ju_Collections_EMPTY_ITERATOR;
},
ju_Collections$3_isEmpty = $this => {
    return 1;
},
ju_Iterator = $rt_classWithoutFields(0),
ju_Collections$4 = $rt_classWithoutFields(),
ju_Collections$4_hasNext = $this => {
    return 0;
},
ju_Collections$4_next = $this => {
    let var$1;
    var$1 = new ju_NoSuchElementException;
    jl_Exception__init_0(var$1);
    $rt_throw(var$1);
},
ju_ListIterator = $rt_classWithoutFields(0),
ju_Collections$5 = $rt_classWithoutFields(),
ju_Collections$_clinit_$lambda$_59_0 = $rt_classWithoutFields(),
jl_StringIndexOutOfBoundsException = $rt_classWithoutFields(jl_IndexOutOfBoundsException),
jl_CloneNotSupportedException = $rt_classWithoutFields(jl_Exception);
function ju_AbstractList$1() {
    let a = this; jl_Object.call(a);
    a.$index = 0;
    a.$modCount0 = 0;
    a.$size1 = 0;
    a.$removeIndex = 0;
    a.$this$0 = null;
}
let ju_AbstractList$1_hasNext = $this => {
    return $this.$index >= $this.$size1 ? 0 : 1;
},
ju_AbstractList$1_next = $this => {
    let var$1, var$2, var$3;
    var$1 = $this.$modCount0;
    var$2 = $this.$this$0;
    if (var$1 != var$2.$modCount) {
        var$2 = new ju_ConcurrentModificationException;
        jl_Exception__init_0(var$2);
        $rt_throw(var$2);
    }
    var$3 = $this.$index;
    $this.$removeIndex = var$3;
    $this.$index = var$3 + 1 | 0;
    return var$2.$get(var$3);
},
jl_AutoCloseable = $rt_classWithoutFields(0),
jus_BaseStream = $rt_classWithoutFields(0),
jus_Stream = $rt_classWithoutFields(0),
jusi_SimpleStreamImpl = $rt_classWithoutFields();
function jusi_StreamOverSpliterator() {
    jusi_SimpleStreamImpl.call(this);
    this.$spliterator = null;
}
let ju_Arrays = $rt_classWithoutFields();
function cc_ClusterOwnershipSelector$Result() {
    let a = this; jl_Record.call(a);
    a.$clusterIndex0 = 0;
    a.$score0 = 0.0;
    a.$memberIndex1 = 0;
}
function cm_ClusterMetric$Result() {
    let a = this; jl_Record.call(a);
    a.$score = 0.0;
    a.$memberIndex = 0;
}
let cm_ClusterMetric$Result__init_0 = ($this, $score, $memberIndex) => {
    $this.$score = $score;
    $this.$memberIndex = $memberIndex;
},
cm_ClusterMetric$Result__init_ = (var_0, var_1) => {
    let var_2 = new cm_ClusterMetric$Result();
    cm_ClusterMetric$Result__init_0(var_2, var_0, var_1);
    return var_2;
},
jus_IntStream = $rt_classWithoutFields(0),
jusi_SimpleIntStreamImpl = $rt_classWithoutFields();
function jusi_MappingToIntStreamImpl() {
    let a = this; jusi_SimpleIntStreamImpl.call(a);
    a.$source = null;
    a.$mapper = null;
}
let ju_Spliterator = $rt_classWithoutFields(0);
function jusi_SpliteratorOverCollection() {
    let a = this; jl_Object.call(a);
    a.$collection = null;
    a.$iterator0 = null;
}
let jusi_SpliteratorOverCollection_ensureIterator = $this => {
    if ($this.$iterator0 === null)
        $this.$iterator0 = ju_AbstractList_iterator($this.$collection);
},
ju_ConcurrentModificationException = $rt_classWithoutFields(jl_RuntimeException),
jlr_Array = $rt_classWithoutFields(),
jlr_Array_newInstance = ($componentType, $length) => {
    if ($componentType === null) {
        $componentType = new jl_NullPointerException;
        jl_Exception__init_0($componentType);
        $rt_throw($componentType);
    }
    if ($componentType === $rt_cls($rt_voidcls)) {
        $componentType = new jl_IllegalArgumentException;
        jl_Exception__init_0($componentType);
        $rt_throw($componentType);
    }
    if ($length < 0) {
        $componentType = new jl_NegativeArraySizeException;
        jl_Exception__init_0($componentType);
        $rt_throw($componentType);
    }
    return otrr_ClassInfo_newArrayInstance($componentType.$classInfo, $length);
},
jl_NegativeArraySizeException = $rt_classWithoutFields(jl_RuntimeException),
juf_IntBinaryOperator = $rt_classWithoutFields(0),
jusi_SimpleIntStreamImpl$min$lambda$_20_0 = $rt_classWithoutFields();
function cm_KthNearestPointDistanceMetric$1DistanceWithIndex() {
    let a = this; jl_Record.call(a);
    a.$distance = 0.0;
    a.$index0 = 0;
}
let cm_KthNearestPointDistanceMetric$1DistanceWithIndex_compareTo = ($this, var$1) => {
    return jl_Double_compare($this.$distance, var$1.$distance);
},
ju_Comparator$NaturalOrder = $rt_classWithoutFields(),
ju_Comparator$NaturalOrder_INSTANCE = null,
ju_Comparator$NaturalOrder_$callClinit = () => {
    ju_Comparator$NaturalOrder_$callClinit = $rt_eraseClinit(ju_Comparator$NaturalOrder);
    ju_Comparator$NaturalOrder__clinit_();
},
ju_Comparator$NaturalOrder_instance = () => {
    ju_Comparator$NaturalOrder_$callClinit();
    return ju_Comparator$NaturalOrder_INSTANCE;
},
ju_Comparator$NaturalOrder__clinit_ = () => {
    let var$1;
    var$1 = new ju_Comparator$NaturalOrder;
    ju_Comparator$NaturalOrder_$callClinit();
    ju_Comparator$NaturalOrder_INSTANCE = var$1;
},
juf_IntPredicate = $rt_classWithoutFields(0);
function jusi_ReducingIntConsumer() {
    let a = this; jl_Object.call(a);
    a.$accumulator = null;
    a.$result = 0;
    a.$initialized = 0;
}
let cm_EllipseDistance = $rt_classWithoutFields(),
juf_Predicate = $rt_classWithoutFields(0);
function jusi_MappingToIntStreamImpl$next$lambda$_1_0() {
    let a = this; jl_Object.call(a);
    a.$_01 = null;
    a.$_1 = null;
}
let juf_Consumer = $rt_classWithoutFields(0);
function jusi_StreamOverSpliterator$AdapterAction() {
    let a = this; jl_Object.call(a);
    a.$consumer = null;
    a.$wantsMore = 0;
}
let jusi_StreamOverSpliterator$AdapterAction_accept = ($this, $t) => {
    let var$2, var$3;
    var$2 = $this.$consumer.$_1;
    var$3 = cm_ClusterSite_size($t);
    $t = var$2;
    if ($t.$initialized)
        $t.$result = jl_Math_min($t.$result, var$3);
    else {
        $t.$result = var$3;
        $t.$initialized = 1;
    }
    $this.$wantsMore = 1;
};
$rt_packages([-1, "java", 0, "lang", -1, "cvdexplorer", 2, "metric", 2, "model"
]);
$rt_metadata([jl_Object, "Object", 1, 0, [], 1, 0, 0, 0,
ji_Serializable, 0, jl_Object, [], 1537, 0, 0, 0,
jl_Comparable, 0, jl_Object, [], 1537, 0, 0, 0,
jl_CharSequence, 0, jl_Object, [], 1537, 0, 0, 0,
jl_String, 0, jl_Object, [ji_Serializable, jl_Comparable, jl_CharSequence], 17, 0, () => jl_String_$callClinit(), ["$toString", $rt_wrapFunction0(jl_String_toString)],
jlr_AnnotatedElement, 0, jl_Object, [], 1537, 0, 0, 0,
jlr_GenericDeclaration, 0, jl_Object, [jlr_AnnotatedElement], 1537, 0, 0, 0,
jlr_Type, 0, jl_Object, [], 1537, 0, 0, 0,
jl_Class, 0, jl_Object, [jlr_GenericDeclaration, jlr_Type], 17, 0, 0, ["$toString", $rt_wrapFunction0(jl_Class_toString)],
jl_Number, 0, jl_Object, [ji_Serializable], 1025, 0, 0, 0,
jl_Integer, 0, jl_Number, [jl_Comparable], 1, 0, () => jl_Integer_$callClinit(), 0,
jl_AbstractStringBuilder, 0, jl_Object, [ji_Serializable, jl_CharSequence], 0, 0, 0, 0,
jl_Appendable, 0, jl_Object, [], 1537, 0, 0, 0,
jl_StringBuilder, 0, jl_AbstractStringBuilder, [jl_Appendable], 1, 0, 0, 0,
jl_Throwable, 0, jl_Object, [], 1, 0, 0, 0,
jl_Exception, 0, jl_Throwable, [], 1, 0, 0, 0,
jl_RuntimeException, 0, jl_Exception, [], 1, 0, 0, 0,
otrr_ReflectionInfo, 0, jl_Object, [], 1025, 0, 0, 0,
otrr_ClassInfo, 0, otrr_ReflectionInfo, [], 17, 0, 0, 0,
otr_StringInfo, 0, otrr_ReflectionInfo, [], 17, 0, 0, 0,
cw_WebClassifyMain, 0, jl_Object, [], 17, 0, () => cw_WebClassifyMain_$callClinit(), 0,
jl_ClassCastException, 0, jl_RuntimeException, [], 1, 0, 0, 0,
otp_Platform, 0, jl_Object, [], 17, 0, 0, 0,
otji_JS, 0, jl_Object, [], 17, 0, 0, 0,
otci_IntegerUtil, 0, jl_Object, [], 17, 0, 0, 0,
ju_Comparator, 0, jl_Object, [], 1537, 0, 0, 0,
jl_String$_clinit_$lambda$_118_0, 0, jl_Object, [ju_Comparator], 1, 0, 0, 0,
jl_Character, 0, jl_Object, [jl_Comparable], 1, 0, () => jl_Character_$callClinit(), 0,
cc_DiagramRasterizer, 0, jl_Object, [], 17, 0, 0, 0,
jl_Iterable, 0, jl_Object, [], 1537, 0, 0, 0,
ju_Collection, 0, jl_Object, [jl_Iterable], 1537, 0, 0, 0,
ju_AbstractCollection, 0, jl_Object, [ju_Collection], 1025, 0, 0, ["$isEmpty", $rt_wrapFunction0(ju_AbstractCollection_isEmpty)],
ju_SequencedCollection, 0, jl_Object, [ju_Collection], 1537, 0, 0, 0,
ju_List, 0, jl_Object, [ju_SequencedCollection], 1537, 0, 0, 0,
ju_AbstractList, 0, ju_AbstractCollection, [ju_List], 1025, 0, 0, ["$iterator", $rt_wrapFunction0(ju_AbstractList_iterator)],
jl_Cloneable, 0, jl_Object, [], 1537, 0, 0, 0,
ju_RandomAccess, 0, jl_Object, [], 1537, 0, 0, 0,
ju_ArrayList, 0, ju_AbstractList, [jl_Cloneable, ji_Serializable, ju_RandomAccess], 1, 0, 0, ["$get", $rt_wrapFunction1(ju_ArrayList_get), "$size", $rt_wrapFunction0(ju_ArrayList_size)],
ju_Objects, 0, jl_Object, [], 17, 0, 0, 0,
otji_JSWrapper, 0, jl_Object, [], 17, 0, 0, 0,
cm_DemoScenes, 0, jl_Object, [], 17, 0, 0, 0,
jl_IndexOutOfBoundsException, 0, jl_RuntimeException, [], 1, 0, 0, 0,
cm_SceneSnapshot, 0, jl_Object, [], 17, 0, 0, 0,
jl_Enum, "Enum", 1, jl_Object, [jl_Comparable, ji_Serializable], 1025, 0, 0, ["$compareTo", $rt_wrapFunction1(jl_Enum_compareTo)],
cm_MetricKind, "MetricKind", 3, jl_Enum, [], 65553, 0, () => cm_MetricKind_$callClinit(), 0,
cm_NeighborOrder, "NeighborOrder", 4, jl_Enum, [], 65553, 0, () => cm_NeighborOrder_$callClinit(), 0,
cm_SiteMemberKind, "SiteMemberKind", 4, jl_Enum, [], 65553, 0, () => cm_SiteMemberKind_$callClinit(), 0,
jl_IllegalArgumentException, 0, jl_RuntimeException, [], 1, 0, 0, 0,
otj_JSObject, 0, jl_Object, [], 1537, 0, 0, 0,
otjc_JSString, 0, jl_Object, [otj_JSObject], 1025, 0, 0, 0]);
$rt_metadata([cm_MetricMemberCompatibility, 0, jl_Object, [], 17, 0, 0, 0,
ju_Optional, 0, jl_Object, [], 17, 0, 0, 0,
cm_ClusterSite, 0, jl_Object, [], 17, 0, 0, 0,
cm_ClusterMember, 0, jl_Object, [], 1537, 0, 0, 0,
c_HandleVisibility, 0, jl_Object, [], 17, 0, 0, 0,
jl_Math, 0, jl_Object, [], 17, 0, 0, 0,
otjc_JSBoolean, 0, jl_Object, [otj_JSObject], 1025, 0, 0, 0,
cg_Vector, 0, jl_Object, [], 17, 0, () => cg_Vector_$callClinit(), 0,
cm_SiteMemberFactory, 0, jl_Object, [], 17, 0, 0, 0,
cw_SceneJsonJs, 0, jl_Object, [], 16, 0, 0, 0,
ci_SceneJsonException, 0, jl_Exception, [], 17, 0, 0, 0,
cc_ScenePreparation, 0, jl_Object, [], 17, 0, () => cc_ScenePreparation_$callClinit(), 0,
cr_ClusterColorizer, 0, jl_Object, [], 17, 0, 0, 0,
jl_Record, 0, jl_Object, [], 1025, 0, 0, 0,
cc_ScenePreparation$PreparedScene, 0, jl_Record, [], 17, 0, 0, 0,
cm_Rgba, 0, jl_Object, [], 17, 0, () => cm_Rgba_$callClinit(), 0,
cg_Box, 0, jl_Object, [], 17, 0, 0, 0,
cc_DiagramRasterizer$Classifier, 0, jl_Object, [], 1537, 0, 0, 0,
cw_WebClassifyMain$computeFrame$lambda$_2_0, 0, jl_Object, [cc_DiagramRasterizer$Classifier], 1, 0, 0, 0,
cc_DiagramRasterizer$Colorizer, 0, jl_Object, [], 1537, 0, 0, 0,
cw_WebClassifyMain$computeFrame$lambda$_2_1, 0, jl_Object, [cc_DiagramRasterizer$Colorizer], 1, 0, 0, 0,
jl_IllegalStateException, 0, jl_RuntimeException, [], 1, 0, 0, 0,
cc_DiagramRasterizer$RasterResult, 0, jl_Record, [], 17, 0, 0, 0,
cc_DiagramRasterizer$OwnershipGrid, 0, jl_Record, [], 17, 0, 0, 0,
cm_PointMember, 0, jl_Record, [cm_ClusterMember], 17, 0, 0, ["$distanceTo", $rt_wrapFunction1(cm_PointMember_distanceTo), "$handleCount", $rt_wrapFunction0(cm_PointMember_handleCount), "$getHandle", $rt_wrapFunction1(cm_PointMember_getHandle), "$withHandle", $rt_wrapFunction2(cm_PointMember_withHandle)],
cm_EllipseMember, 0, jl_Object, [cm_ClusterMember], 17, 0, 0, ["$distanceTo", $rt_wrapFunction1(cm_EllipseMember_distanceTo), "$handleCount", $rt_wrapFunction0(cm_EllipseMember_handleCount), "$getHandle", $rt_wrapFunction1(cm_EllipseMember_getHandle), "$withHandle", $rt_wrapFunction2(cm_EllipseMember_withHandle)],
cm_SegmentMember, 0, jl_Record, [cm_ClusterMember], 17, 0, 0, ["$distanceTo", $rt_wrapFunction1(cm_SegmentMember_distanceTo), "$handleCount", $rt_wrapFunction0(cm_SegmentMember_handleCount), "$getHandle", $rt_wrapFunction1(cm_SegmentMember_getHandle), "$withHandle", $rt_wrapFunction2(cm_SegmentMember_withHandle)],
cm_LineMember, 0, jl_Record, [cm_ClusterMember], 17, 0, 0, ["$distanceTo", $rt_wrapFunction1(cm_LineMember_distanceTo), "$handleCount", $rt_wrapFunction0(cm_LineMember_handleCount), "$getHandle", $rt_wrapFunction1(cm_LineMember_getHandle), "$withHandle", $rt_wrapFunction2(cm_LineMember_withHandle)],
ju_NoSuchElementException, 0, jl_RuntimeException, [], 1, 0, 0, 0,
cm_ClusterNaming, 0, jl_Object, [], 17, 0, () => cm_ClusterNaming_$callClinit(), 0,
cm_CircleMember, 0, jl_Record, [cm_ClusterMember], 17, 0, 0, ["$distanceTo", $rt_wrapFunction1(cm_CircleMember_distanceTo), "$handleCount", $rt_wrapFunction0(cm_CircleMember_handleCount), "$getHandle", $rt_wrapFunction1(cm_CircleMember_getHandle), "$withHandle", $rt_wrapFunction2(cm_CircleMember_withHandle)],
cm_SiteMemberFactory$1, 0, jl_Object, [], 32768, 0, () => cm_SiteMemberFactory$1_$callClinit(), 0,
jl_MatchException, 0, jl_RuntimeException, [], 17, 0, 0, 0,
ci_SceneFileFormat, 0, jl_Object, [], 17, 0, 0, 0,
cg_Transformation, 0, jl_Object, [], 17, 0, () => cg_Transformation_$callClinit(), 0,
cc_DiagramRasterizer$GridSpec, 0, jl_Record, [], 16, 0, 0, 0,
jl_Double, 0, jl_Number, [jl_Comparable], 1, 0, () => jl_Double_$callClinit(), ["$compareTo", $rt_wrapFunction1(jl_Double_compareTo)],
ju_TemplateCollections$AbstractImmutableList, 0, ju_AbstractList, [ju_RandomAccess], 1024, 0, 0, 0,
ju_TemplateCollections$TwoElementsList, 0, ju_TemplateCollections$AbstractImmutableList, [ju_RandomAccess], 0, 0, 0, ["$size", $rt_wrapFunction0(ju_TemplateCollections$TwoElementsList_size), "$get", $rt_wrapFunction1(ju_TemplateCollections$TwoElementsList_get)],
ju_TemplateCollections$ImmutableArrayList, 0, ju_TemplateCollections$AbstractImmutableList, [ju_RandomAccess], 1, 0, 0, ["$get", $rt_wrapFunction1(ju_TemplateCollections$ImmutableArrayList_get), "$size", $rt_wrapFunction0(ju_TemplateCollections$ImmutableArrayList_size)],
ju_Collections, 0, jl_Object, [], 17, 0, () => ju_Collections_$callClinit(), 0,
ci_SceneFileFormat$SceneFileV1, 0, jl_Object, [], 17, 0, 0, 0,
ci_SceneFileFormat$ClusterJson, 0, jl_Object, [], 17, 0, 0, 0,
ci_SceneFileFormat$ColorJson, 0, jl_Object, [], 17, 0, 0, 0,
cm_ClusterMetric, 0, jl_Object, [], 1537, 0, 0, 0,
cm_NearestMemberMetric, 0, jl_Object, [cm_ClusterMetric], 17, 0, 0, ["$evaluate0", $rt_wrapFunction2(cm_NearestMemberMetric_evaluate)],
cm_FarthestMemberMetric, 0, jl_Object, [cm_ClusterMetric], 17, 0, 0, ["$evaluate0", $rt_wrapFunction2(cm_FarthestMemberMetric_evaluate)],
cm_SumOfDistancesMetric, 0, jl_Object, [cm_ClusterMetric], 17, 0, 0, ["$evaluate0", $rt_wrapFunction2(cm_SumOfDistancesMetric_evaluate)],
cm_MeanOfDistancesMetric, 0, jl_Object, [cm_ClusterMetric], 17, 0, () => cm_MeanOfDistancesMetric_$callClinit(), ["$evaluate0", $rt_wrapFunction2(cm_MeanOfDistancesMetric_evaluate)],
cc_ClusterOwnershipSelector, 0, jl_Object, [], 17, 0, 0, 0]);
$rt_metadata([cc_ScenePreparation$1, 0, jl_Object, [], 32768, 0, () => cc_ScenePreparation$1_$callClinit(), 0,
juf_ToIntFunction, 0, jl_Object, [], 1537, 0, 0, 0,
cc_ScenePreparation$metricFor$lambda$_2_0, 0, jl_Object, [juf_ToIntFunction], 1, 0, 0, 0,
cm_KthNearestPointDistanceMetric, 0, jl_Object, [cm_ClusterMetric], 17, 0, 0, ["$evaluate0", $rt_wrapFunction2(cm_KthNearestPointDistanceMetric_evaluate)],
jl_NullPointerException, 0, jl_RuntimeException, [], 1, 0, 0, 0,
cc_DiagramRasterizer$Classification, 0, jl_Record, [], 17, 0, 0, 0,
ju_TemplateCollections$SingleElementList, 0, ju_TemplateCollections$AbstractImmutableList, [ju_RandomAccess], 0, 0, 0, ["$size", $rt_wrapFunction0(ju_TemplateCollections$SingleElementList_size), "$get", $rt_wrapFunction1(ju_TemplateCollections$SingleElementList_get)],
ci_SceneFileFormat$MemberJson, 0, jl_Object, [], 17, 0, 0, 0,
ju_OptionalInt, 0, jl_Object, [], 1, 0, 0, 0,
ju_Set, 0, jl_Object, [ju_Collection], 1537, 0, 0, 0,
ju_AbstractSet, 0, ju_AbstractCollection, [ju_Set], 1025, 0, 0, 0,
ju_TemplateCollections$AbstractImmutableSet, 0, ju_AbstractSet, [], 1024, 0, 0, 0,
ju_Collections$1, 0, ju_TemplateCollections$AbstractImmutableSet, [], 0, 0, 0, 0,
ju_Map, 0, jl_Object, [], 1537, 0, 0, 0,
ju_AbstractMap, 0, jl_Object, [ju_Map], 1025, 0, 0, 0,
ju_TemplateCollections$AbstractImmutableMap, 0, ju_AbstractMap, [], 1024, 0, 0, 0,
ju_Collections$2, 0, ju_TemplateCollections$AbstractImmutableMap, [], 0, 0, 0, 0,
ju_Collections$3, 0, ju_TemplateCollections$AbstractImmutableList, [], 0, 0, 0, ["$get", $rt_wrapFunction1(ju_Collections$3_get), "$size", $rt_wrapFunction0(ju_Collections$3_size), "$iterator", $rt_wrapFunction0(ju_Collections$3_iterator), "$isEmpty", $rt_wrapFunction0(ju_Collections$3_isEmpty)],
ju_Iterator, 0, jl_Object, [], 1537, 0, 0, 0,
ju_Collections$4, 0, jl_Object, [ju_Iterator], 0, 0, 0, ["$hasNext", $rt_wrapFunction0(ju_Collections$4_hasNext), "$next", $rt_wrapFunction0(ju_Collections$4_next)],
ju_ListIterator, 0, jl_Object, [ju_Iterator], 1537, 0, 0, 0,
ju_Collections$5, 0, jl_Object, [ju_ListIterator], 0, 0, 0, 0,
ju_Collections$_clinit_$lambda$_59_0, 0, jl_Object, [ju_Comparator], 1, 0, 0, 0,
jl_StringIndexOutOfBoundsException, 0, jl_IndexOutOfBoundsException, [], 1, 0, 0, 0,
jl_CloneNotSupportedException, 0, jl_Exception, [], 1, 0, 0, 0,
ju_AbstractList$1, 0, jl_Object, [ju_Iterator], 0, 0, 0, ["$hasNext", $rt_wrapFunction0(ju_AbstractList$1_hasNext), "$next", $rt_wrapFunction0(ju_AbstractList$1_next)],
jl_AutoCloseable, 0, jl_Object, [], 1537, 0, 0, 0,
jus_BaseStream, 0, jl_Object, [jl_AutoCloseable], 1537, 0, 0, 0,
jus_Stream, 0, jl_Object, [jus_BaseStream], 1537, 0, 0, 0,
jusi_SimpleStreamImpl, 0, jl_Object, [jus_Stream], 1025, 0, 0, 0,
jusi_StreamOverSpliterator, 0, jusi_SimpleStreamImpl, [], 1, 0, 0, 0,
ju_Arrays, 0, jl_Object, [], 1, 0, 0, 0,
cc_ClusterOwnershipSelector$Result, 0, jl_Record, [], 17, 0, 0, 0,
cm_ClusterMetric$Result, 0, jl_Record, [], 17, 0, 0, 0,
jus_IntStream, 0, jl_Object, [jus_BaseStream], 1537, 0, 0, 0,
jusi_SimpleIntStreamImpl, 0, jl_Object, [jus_IntStream], 1025, 0, 0, 0,
jusi_MappingToIntStreamImpl, 0, jusi_SimpleIntStreamImpl, [], 1, 0, 0, 0,
ju_Spliterator, 0, jl_Object, [], 1537, 0, 0, 0,
jusi_SpliteratorOverCollection, 0, jl_Object, [ju_Spliterator], 1, 0, 0, 0,
ju_ConcurrentModificationException, 0, jl_RuntimeException, [], 1, 0, 0, 0,
jlr_Array, 0, jl_Object, [], 17, 0, 0, 0,
jl_NegativeArraySizeException, 0, jl_RuntimeException, [], 1, 0, 0, 0,
juf_IntBinaryOperator, 0, jl_Object, [], 1537, 0, 0, 0,
jusi_SimpleIntStreamImpl$min$lambda$_20_0, 0, jl_Object, [juf_IntBinaryOperator], 1, 0, 0, 0,
cm_KthNearestPointDistanceMetric$1DistanceWithIndex, 0, jl_Record, [jl_Comparable], 16, 0, 0, ["$compareTo", $rt_wrapFunction1(cm_KthNearestPointDistanceMetric$1DistanceWithIndex_compareTo)],
ju_Comparator$NaturalOrder, 0, jl_Object, [ju_Comparator], 1, 0, () => ju_Comparator$NaturalOrder_$callClinit(), 0,
juf_IntPredicate, 0, jl_Object, [], 1537, 0, 0, 0,
jusi_ReducingIntConsumer, 0, jl_Object, [juf_IntPredicate], 0, 0, 0, 0,
cm_EllipseDistance, 0, jl_Object, [], 16, 0, 0, 0,
juf_Predicate, 0, jl_Object, [], 1537, 0, 0, 0]);
$rt_metadata([jusi_MappingToIntStreamImpl$next$lambda$_1_0, 0, jl_Object, [juf_Predicate], 1, 0, 0, 0,
juf_Consumer, 0, jl_Object, [], 1537, 0, 0, 0,
jusi_StreamOverSpliterator$AdapterAction, 0, jl_Object, [juf_Consumer], 0, 0, 0, 0]);
$rt_enumConstantsMetadata([
    cm_MetricKind, () => [cm_MetricKind_MINIMUM_DISTANCE, cm_MetricKind_MAXIMUM_DISTANCE, cm_MetricKind_SUM_OF_DISTANCES, cm_MetricKind_MEAN_DISTANCE, cm_MetricKind_KTH_NEAREST_DISTANCE], cm_NeighborOrder, () => [cm_NeighborOrder_NEAREST, cm_NeighborOrder_FARTHEST], cm_SiteMemberKind, () => [cm_SiteMemberKind_POINT, cm_SiteMemberKind_LINE_SEGMENT, cm_SiteMemberKind_CIRCLE, cm_SiteMemberKind_ELLIPSE, cm_SiteMemberKind_LINE]]);
let $rt_booleanArrayCls = $rt_arraycls($rt_booleancls),
$rt_charArrayCls = $rt_arraycls($rt_charcls),
$rt_intArrayCls = $rt_arraycls($rt_intcls),
$rt_doubleArrayCls = $rt_arraycls($rt_doublecls);
$rt_stringPool(["0", "null", "interface ", "class ", "", "[L", "POINT", "SEGMENT", "CIRCLE", "LINE", "ELLIPSE", "Already at the maximum number of clusters", "Cannot remove the last cluster", "No clusters", "Cannot remove the last member of a cluster", "Metric parameter k must be between 1 and 32", "No clusters to add a member to", "Active cluster already has the maximum number of members", "Unknown neighbor order: ", "World view requires max > min on both axes", "Invalid scene JSON", "Scene JSON is empty", "Active cluster index out of range",
"Unknown metric: ", "rasterizer returned null", "width and height must be positive", "Unknown site member kind: ", "Amber", "Azure", "Rose", "Lime", "Index out of range: ", "Class does not represent enum", "Enum ", " does not have the ", " constant", "Can\'t compare ", " to ", "MINIMUM_DISTANCE", "MAXIMUM_DISTANCE", "SUM_OF_DISTANCES", "MEAN_DISTANCE", "KTH_NEAREST_DISTANCE", "NEAREST", "FARTHEST", "LINE_SEGMENT", " is only supported for clusters made entirely of points.", "Invalid JSON", "Empty scene file",
"version", "name", "metricKind", "neighborOrder", "siteMemberKind", "nearestNeighborK", "Scene must contain at least one cluster", "Cluster entry is null", "r", "g", "b", "opacity", "Cluster must have at least one member: ", "kind", "x", "y", "ax", "ay", "bx", "by", "cx", "cy", "radius", "px", "py", "qx", "qy", "hx", "hy", "Red", "Orange", "Yellow", "Green", "Teal", "Cyan", "Blue", "Indigo", "Violet", "Magenta", "1", "Too many clusters (max 32)", "AVERAGE_DISTANCE", "Unknown metricKind: ", "Unknown neighborOrder: ",
"Unknown siteMemberKind: ", "nearestNeighborK must be between 1 and 32", "Cluster color is required for ", "Cluster name is required", "metricKind is required", "Unsupported or missing version (expected 1)", "Too many members in cluster ", "Member kind is required in cluster ", "Unknown member kind: ", " in cluster ", "CIRCLE member radius must be non-negative in cluster ", "POINT member requires x and y in cluster ", "LINE_SEGMENT member requires ax, ay, bx, by in cluster ", "ELLIPSE member requires ax, ay, bx, by, hx, hy in cluster ",
"CIRCLE member requires cx, cy, radius in cluster ", "LINE member requires px, py, qx, qy in cluster "]);
jl_String.prototype.toString = function() {
    return $rt_ustr(this);
};
jl_String.prototype.valueOf = jl_String.prototype.toString;
jl_Object.prototype.toString = function() {
    return $rt_ustr(jl_Object_toString(this));
};
jl_Object.prototype.__teavm_class__ = function() {
    return $dbg_class(this);
};
let $rt_export_main = $rt_mainStarter(cw_WebClassifyMain_main);
$rt_export_main.javaException = $rt_javaException;
export { $rt_export_main as main };
