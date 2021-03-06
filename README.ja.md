[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![CircleCI](https://circleci.com/gh/ProjectMapK/KMapper.svg?style=svg)](https://circleci.com/gh/ProjectMapK/KMapper)
[![](https://jitci.com/gh/ProjectMapK/KMapper/svg)](https://jitci.com/gh/ProjectMapK/KMapper)
[![codecov](https://codecov.io/gh/ProjectMapK/KMapper/branch/master/graph/badge.svg)](https://codecov.io/gh/ProjectMapK/KMapper)

KMapper
====
`KMapper`は`Kotlin`向けのマッパーライブラリであり、以下の機能を提供します。  

- オブジェクトや`Map`、`Pair`をソースとした`Bean`マッピング
- `Kotlin`のリフレクションを用いた関数呼び出しベースの安全なマッピング
- 豊富な機能による、より柔軟かつ労力の少ないマッピング

以下のリポジトリに簡単なベンチマーク結果を掲載しています。

- [ProjectMapK/MapKInspections: Testing and benchmarking for ProjectMapK deliverables\.](https://github.com/ProjectMapK/MapKInspections#results)

## デモコード
手動でマッピングコードを書いた場合と`KMapper`を用いた場合を比較します。  
手動で書く場合引数が多ければ多いほど記述がかさみますが、`KMapper`を用いることで殆どコードを書かずにマッピングを行えます。  
また、外部の設定ファイルは一切必要ありません。

```kotlin
// 手動でマッピングを行う場合
val dst = Dst(
    param1 = src.param1,
    param2 = src.param2,
    param3 = src.param3,
    param4 = src.param4,
    param5 = src.param5,
    ...
)

// KMapperを用いる場合
val dst = KMapper(::Dst).map(src)
```

ソースは1つに限らず、複数のオブジェクトや、`Pair`、`Map`等を指定することもできます。

```kotlin
val dst = KMapper(::Dst).map(
    "param1" to "value of param1",
    mapOf("param2" to 1, "param3" to 2L),
    src1,
    src2
)
```

## インストール方法
`KMapper`は`JitPack`にて公開しており、`Maven`や`Gradle`といったビルドツールから手軽に利用できます。  
各ツールでの正確なインストール方法については下記をご参照ください。

- [ProjectMapK / KMapper](https://jitpack.io/#ProjectMapK/KMapper)

### Mavenでのインストール方法
以下は`Maven`でのインストール例です。

**1. JitPackのリポジトリへの参照を追加する**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**2. dependencyを追加する**

```xml
<dependency>
    <groupId>com.github.ProjectMapK</groupId>
    <artifactId>KMapper</artifactId>
    <version>Tag</version>
</dependency>
```

## 動作原理
`KMapper`は以下のように動作します。

1. 呼び出し対象の`KFunction`を取り出す
2. `KFunction`を解析し、必要な引数とその取り出し方を決定する
3. 入力からそれぞれの引数に対応する値の取り出しを行い、`KFunction`を呼び出す

最終的にはコンストラクタや`companion object`に定義したファクトリーメソッドなどを呼び出してマッピングを行うため、結果は`Kotlin`上の引数・`nullability`等の制約に従います。  
つまり、`Kotlin`の`null`安全が壊れることによる実行時エラーは発生しません（ただし、型引数の`nullability`に関しては`null`安全が壊れる場合が有ります）。

また、`Kotlin`特有の機能であるデフォルト引数等にも対応しています。

## マッパークラスの種類について
このプロジェクトでは以下の3種類のマッパークラスを提供しています。

- `KMapper`
- `PlainKMapper`
- `BoundKMapper`

以下にそれぞれの特徴と使いどころをまとめます。  
また、これ以降共通の機能に関しては`KMapper`を例に説明を行います。

### KMapper
`KMapper`はこのプロジェクトの基本となるマッパークラスです。  
内部ではキャッシュを用いたマッピングの高速化などを行っているため、マッパーを使い回す形での利用に向きます。

### PlainKMapper
`PlainKMapper`は`KMapper`からキャッシュ機能を取り除いたマッパークラスです。  
複数回マッピングを行った場合の性能は`KMapper`に劣りますが、キャッシュ処理のオーバーヘッドが無いため、マッパーを使い捨てる形での利用に向きます。

### BoundKMapper
`BoundKMapper`はソースとなるクラスが1つに限定できる場合に利用できるマッピングクラスです。  
`KMapper`に比べ高速に動作します。

## KMapperの初期化
`KMapper`は呼び出し対象の`method reference(KFunction)`、またはマッピング先の`KClass`から初期化できます。  

以下にそれぞれの初期化方法をまとめます。  
ただし、`BoundKMapper`の初期化の内可能なものは全てダミーコンストラクタによって簡略化した例を示します。

### method reference(KFunction)からの初期化
プライマリコンストラクタを呼び出し対象とする場合、以下のように初期化を行うことができます。

```kotlin
data class Dst(
    foo: String,
    bar: String,
    baz: Int?,

    ...

)

// コンストラクタのメソッドリファレンスを取得
val dstConstructor: KFunction<Dst> = ::Dst

// KMapperの場合
val kMapper: KMapper<Dst> = KMapper(dstConstructor)
// PlainKMapperの場合
val plainMapper: PlainKMapper<Dst> = PlainKMapper(dstConstructor)
// BoundKMapperの場合
val boundKMapper: BoundKMapper<Src, Dst> = BoundKMapper(dstConstructor)
```

### KClassからの初期化
`KMapper`は`KClass`からも初期化できます。  
デフォルトではプライマリーコンストラクタが呼び出し対象になります。

```kotlin
data class Dst(...)

// KMapperの場合
val kMapper: KMapper<Dst> = KMapper(Dst::class)
// PlainKMapperの場合
val plainMapper: PlainKMapper<Dst> = PlainKMapper(Dst::class)
// BoundKMapperの場合
val boundKMapper: BoundKMapper<Src, Dst> = BoundKMapper(Dst::class, Src::class)
```

ダミーコンストラクタを用い、かつジェネリクスを省略することで、それぞれ以下のようにも書けます。

```kotlin
// KMapperの場合
val kMapper: KMapper<Dst> = KMapper()
// PlainKMapperの場合
val plainMapper: PlainKMapper<Dst> = PlainKMapper()
// BoundKMapperの場合
val boundKMapper: BoundKMapper<Src, Dst> = BoundKMapper()
```

#### KConstructorアノテーションによる呼び出し対象指定
`KClass`から初期化を行う場合、全てのマッパークラスでは`KConstructor`アノテーションを用いて呼び出し対象の関数を指定することができます。  

以下の例ではセカンダリーコンストラクタが呼び出されます。

```kotlin
data class Dst(...) {
    @KConstructor
    constructor(...) : this(...)
}

val mapper: KMapper<Dst> = KMapper(Dst::class)
```

同様に、以下の例ではファクトリーメソッドが呼び出されます。

```kotlin
data class Dst(...) {
    companion object {
        @KConstructor
        fun factory(...): Dst {
            ...
        }
    }
}

val mapper: KMapper<Dst> = KMapper(Dst::class)
```

## 詳細な使い方

### マッピング時の値の変換
マッピングを行うに当たり、入力の型を別の型に変換したい場合が有ります。  
`KMapper`では、そのような状況に対応するため、豊富な変換機能を提供しています。

ただし、この変換処理は以下の条件でのみ行われます。

- 入力が非`null`
  - `null`が絡む場合は`KParameterRequireNonNull`アノテーションとデフォルト引数を組み合わせることを推奨します
- 入力が引数に直接代入できない

#### デフォルトで利用可能な変換
いくつかの変換機能は、特別な記述無しに利用することができます。

##### 1対1変換（ネストしたマッピング）
引数をそのまま用いることができず、かつその他の変換も行えない場合、`KMapper`は内部でマッピングクラスを用い、1対1マッピングを試みます。  
これによって、デフォルトで以下のようなネストしたマッピングを行うことができます。

```kotlin
data class InnerDst(val foo: Int, val bar: Int)
data class Dst(val param: InnerDst)

data class InnerSrc(val foo: Int, val bar: Int)
data class Src(val param: InnerSrc)

val src = Src(InnerSrc(1, 2))
val dst = KMapper(::Dst).map(src)

println(dst.param) // -> InnerDst(foo=1, bar=2)
```

###### ネストしたマッピングに用いられる関数の指定
ネストしたマッピングは、`BoundKMapper`をクラスから初期化して用いることで行われます。  
このため、`KConstructor`アノテーションを用いて呼び出し対象を指定することができます。

##### その他の変換

###### StringからEnumへの変換
入力が`String`で、かつ引数が`Enum`だった場合、入力と対応する`name`を持つ`Enum`への変換が試みられます。  

```kotlin
enum class FizzBuzz {
    Fizz, Buzz, FizzBuzz;
}

data class Dst(val fizzBuzz: FizzBuzz)

val dst = KMapper(::Dst).map("fizzBuzz" to "Fizz")
println(dst) // -> Dst(fizzBuzz=Fizz)
```

###### Stringへの変換
引数が`String`だった場合、入力を`toString`する変換が行われます。

#### KConverterアノテーションを設定することによる変換
自作のクラスで、かつ単一引数から初期化できる場合、`KConverter`アノテーションを用いた変換が利用できます。  
`KConverter`アノテーションは、コンストラクタ、もしくは`companion object`に定義したファクトリーメソッドに対して付与できます。

```kotlin
// プライマリーコンストラクタに付与した場合
data class FooId @KConverter constructor(val id: Int)
```

```kotlin
// セカンダリーコンストラクタに付与した場合
data class FooId(val id: Int) {
    @KConverter
    constructor(id: String) : this(id.toInt())
}
```

```kotlin
// ファクトリーメソッドに付与した場合
data class FooId(val id: Int) {
    companion object {
        @KConverter
        fun of(id: String): FooId = FooId(id.toInt())
    }
}
```

```kotlin
// fooIdにKConverterが付与されていればDstでは何もせずに正常にマッピングができる
data class Dst(
    fooId: FooId,
    bar: String,
    baz: Int?,

    ...

)
```

#### コンバートアノテーションを自作しての変換
1対1の変換で`KConverter`を用いることができない場合、コンバートアノテーションを自作してパラメータに付与することで変換を行うことができます。

コンバートアノテーションの自作はコンバートアノテーションとコンバータの組を定義することで行います。  
例として`java.sql.Timestamp`もしくは`java.time.Instant`から指定したタイムゾーンの`ZonedDateTime`に変換を行う`ZonedDateTimeConverter`の作成の様子を示します。

##### コンバートアノテーションを定義する
`@Target(AnnotationTarget.VALUE_PARAMETER)`と`KConvertBy`アノテーション、他幾つかのアノテーションを付与することで、コンバートアノテーションを定義できます。

`KConvertBy`アノテーションの引数は、後述するコンバーターの`KClass`を渡します。  
このコンバーターはソースとなる型ごとに定義する必要があります。

また、この例ではアノテーションに引数を定義していますが、この値はコンバーターから参照することができます。

```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@KConvertBy([TimestampToZonedDateTimeConverter::class, InstantToZonedDateTimeConverter::class])
annotation class ZonedDateTimeConverter(val zoneIdOf: String)
```

##### コンバーターを定義する
コンバーターは`AbstractKConverter<A, S, D>`を継承して定義します。  
ジェネリクス`A`,`S`,`D`はそれぞれ以下の意味が有ります。
- `A`: コンバートアノテーションの`Type`
- `S`: 変換前の`Type`
- `D`: 変換後の`Type`

以下は`java.sql.Timestamp`から`ZonedDateTime`へ変換を行うコンバーターの例です。

```kotlin
class TimestampToZonedDateTimeConverter(
    annotation: ZonedDateTimeConverter
) : AbstractKConverter<ZonedDateTimeConverter, Timestamp, ZonedDateTime>(annotation) {
    private val timeZone = ZoneId.of(annotation.zoneIdOf)

    override val srcClass: KClass<Timestamp> = Timestamp::class

    override fun convert(source: Timestamp): ZonedDateTime = ZonedDateTime.of(source.toLocalDateTime(), timeZone)
}
```

コンバーターのプライマリコンストラクタの引数はコンバートアノテーションのみ取る必要が有ります。  
これは`KMapper`の初期化時に呼び出されます。

例の通り、アノテーションに定義した引数は適宜参照することができます。

##### 付与する
ここまでで定義したコンバートアノテーションとコンバーターをまとめて書くと以下のようになります。  
`InstantToZonedDateTimeConverter`は`java.time.Instant`をソースとするコンバーターです。

```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@KConvertBy([TimestampToZonedDateTimeConverter::class, InstantToZonedDateTimeConverter::class])
annotation class ZonedDateTimeConverter(val zoneIdOf: String)

class TimestampToZonedDateTimeConverter(
    annotation: ZonedDateTimeConverter
) : AbstractKConverter<ZonedDateTimeConverter, Timestamp, ZonedDateTime>(annotation) {
    private val timeZone = ZoneId.of(annotation.zoneIdOf)

    override val srcClass: KClass<Timestamp> = Timestamp::class

    override fun convert(source: Timestamp): ZonedDateTime = ZonedDateTime.of(source.toLocalDateTime(), timeZone)
}

class InstantToZonedDateTimeConverter(
    annotation: ZonedDateTimeConverter
) : AbstractKConverter<ZonedDateTimeConverter, Instant, ZonedDateTime>(annotation) {
    private val timeZone = ZoneId.of(annotation.zoneIdOf)

    override val srcClass: KClass<Instant> = Instant::class

    override fun convert(source: Instant): ZonedDateTime = ZonedDateTime.ofInstant(source, timeZone)
}
```

これを付与すると以下のようになります。

```kotlin
data class Dst(
    @ZonedDateTimeConverter("Asia/Tokyo")
    val t1: ZonedDateTime,
    @ZonedDateTimeConverter("-03:00")
    val t2: ZonedDateTime
)
```

#### 複数引数からの変換
以下のような`Dst`で、`InnerDst`をマップ元の複数のフィールドから変換したい場合、`KParameterFlatten`アノテーションが利用できます。

```kotlin
data class InnerDst(val fooFoo: Int, val barBar: String)
data class Dst(val bazBaz: InnerDst, val quxQux: LocalDateTime)
```

`Dst`のフィールド名をプレフィックスに指定する場合以下のように付与します。  
ここで、`KParameterFlatten`を指定されたクラスは、前述の`KConstructor`アノテーションで指定した関数またはプライマリコンストラクタから初期化されます。

```kotlin
data class InnerDst(val fooFoo: Int, val barBar: String)
data class Dst(
    @KParameterFlatten
    val bazBaz: InnerDst,
    val quxQux: LocalDateTime
)
data class Src(val bazBazFooBoo: Int, val bazBazBarBar: String, val quxQux: LocalDateTime)

// bazBazFooFoo, bazBazBarBar, quxQuxの3引数が要求される
val mapper = KMapper(::Dst)
```

##### KParameterFlattenアノテーションのオプション
`KParameterFlatten`アノテーションはネストしたクラスの引数名の扱いについて2つのオプションを持ちます。

###### fieldNameToPrefix
`KParameterFlatten`アノテーションはデフォルトでは引数名をプレフィックスに置いた名前で一致を見ようとします。  
引数名をプレフィックスに付けたくない場合は`fieldNameToPrefix`オプションに`false`を指定します。

```kotlin
data class InnerDst(val fooFoo: Int, val barBar: String)
data class Dst(
    @KParameterFlatten(fieldNameToPrefix = false)
    val bazBaz: InnerDst,
    val quxQux: LocalDateTime
)

// fooFoo, barBar, quxQuxの3引数が要求される
val mapper = KMapper(::Dst)
```

`fieldNameToPrefix = false`を指定した場合、`nameJoiner`オプションは無視されます。

###### nameJoiner
`nameJoiner`は引数名と引数名の結合方法の指定です。  
例えば`Src`が`snake_case`だった場合、以下のように利用します。

```kotlin
data class InnerDst(val fooFoo: Int, val barBar: String)
data class Dst(
    @KParameterFlatten(nameJoiner = NameJoiner.Snake::class)
    val bazBaz: InnerDst,
    val quxQux: LocalDateTime
)

// baz_baz_foo_foo, baz_baz_bar_bar, qux_quxの3引数が要求される
val mapper = KMapper(::Dst) { /* キャメル -> スネークの命名変換関数 */ }
```

デフォルトでは`camelCase`が指定されており、`snake_case`と`kebab-case`のサポートも有ります。  
`NameJoiner`クラスを継承した`object`を作成することで自作することもできます。

##### 他の変換方法との併用
`KParameterFlatten`アノテーションを付与した場合も、これまでに紹介した変換方法は全て機能します。  
また、`KParameterFlatten`アノテーションは何重にネストした中でも利用が可能です。

### マッピング時に用いる引数名・フィールド名の設定
`KMapper`は、デフォルトでは引数名に対応する名前のフィールドをソースからそのまま探します。  
一方、引数名とソースで違う名前を用いたいという場合も有ります。

`KMapper`では、そのような状況に対応するため、マッピング時に用いる引数名・フィールド名を設定するいくつかの機能を提供しています。

#### 引数名の変換
`KMapper`では、初期化時に引数名の変換関数を設定することができます。  
例えば引数の命名規則がキャメルケースかつソースの命名規則がスネークケースというような、一定の変換が要求される状況に対応することができます。

```kotlin
data class Dst(
    fooFoo: String,
    barBar: String,
    bazBaz: Int?
)

val mapper: KMapper<Dst> = KMapper(::Dst) { fieldName: String ->
    /* 命名変換処理 */
}

// 例えばスネークケースへの変換関数を渡すことで、以下のような入力にも対応できる
val dst = mapper.map(mapOf(
    "foo_foo" to "foo",
    "bar_bar" to "bar",
    "baz_baz" to 3
))
```

また、当然ながらラムダ内で任意の変換処理を行うこともできます。

##### 引数名の変換処理の伝播について
引数名の変換処理は、ネストしたマッピングにも反映されます。  
また、後述する`KParameterAlias`アノテーションで指定したエイリアスに関しても変換が適用されます。

##### 実際の変換処理
`KMapper`では命名変換処理を提供していませんが、プロジェクトでよく用いられるライブラリでも命名変換処理が提供されている場合が有ります。  
`Jackson`、`Guava`の2つのライブラリで実際に「キャメルケース -> スネークケース」の変換処理を渡すサンプルコードを示します。

###### Jackson
```kotlin
import com.fasterxml.jackson.databind.PropertyNamingStrategy

val parameterNameConverter: (String) -> String = PropertyNamingStrategy.SnakeCaseStrategy()::translate
val mapper: KMapper<Dst> = KMapper(::Dst, parameterNameConverter)
```

###### Guava
```kotlin
import com.google.common.base.CaseFormat

val parameterNameConverter: (String) -> String = { fieldName: String ->
    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName)
}
val mapper: KMapper<Dst> = KMapper(::Dst, parameterNameConverter)
```

#### ゲッターにエイリアスを設定する
以下のようなコードで、マッピング時にのみ`Scr`クラスの`_foo`フィールドの名前を変更する場合、`KGetterAlias`アノテーションを用いるのが最適です。

```kotlin
data class Dst(val foo: Int)
data class Src(val _foo: Int)
```

実際に付与すると以下のようになります。

```kotlin
data class Src(
    @get:KGetterAlias("foo")
    val _foo: Int
)
```

#### 引数名にエイリアスを設定する
以下のようなコードで、マッピング時にのみ`Dst`クラスの`_bar`フィールドの名前を変更する場合、`KParameterAlias`アノテーションを用いるのが最適です。

```kotlin
data class Dst(val _bar: Int)
data class Src(val bar: Int)
```

実際に付与すると以下のようになります。

```kotlin
data class Dst(
    @KParameterAlias("bar")
    val _bar: Int
)
```

### その他機能
#### 制御してデフォルト引数を用いる
`KMapper`では、引数が指定されていなかった場合デフォルト引数を用います。  
また、引数が指定されていた場合でも、それを用いるか制御することができます。

##### 必ずデフォルト引数を用いる
必ずデフォルト引数を用いたい場合、`KUseDefaultArgument`アノテーションを利用できます。

```kotlin
class Foo(
    ...,
    @KUseDefaultArgument
    val description: String = ""
)
```

##### 対応する内容が全てnullの場合デフォルト引数を用いる
`KParameterRequireNonNull`アノテーションを指定することで、引数として`non null`な値が指定されるまで入力をスキップします。  
これを利用することで、対応する内容が全て`null`の場合デフォルト引数を用いるという挙動が実現できます。

```kotlin
class Foo(
    ...,
    @KParameterRequireNonNull
    val description: String = ""
)
```

#### マッピング時にフィールドを無視する
何らかの理由でマッピング時にフィールドを無視したい場合、`KGetterIgnore`アノテーションを用いることができます。  
例えば、以下の`Src`クラスを入力した場合、`param1`フィールドは読み出し処理が行われません。

```kotlin
data class Src(
    @KGetterIgnore
    val param1: Int,
    val param2: Int
)
```

## 引数のセットアップ

### 引数読み出しの対象
`KMapper`は、オブジェクトの`public`フィールド、もしくは`Pair<String, Any?>`、`Map<String, Any?>`のプロパティを読み出しの対象とすることができます。

### 引数のセットアップ
`KMapper`は、値が`null`でなければセットアップ処理を行います。  
セットアップ処理では、まず`parameterClazz.isSuperclassOf(inputClazz)`で入力が引数に設定可能かを判定し、そのままでは設定できない場合は後述する変換処理を行い、結果を引数とします。

値が`null`だった場合は`KParameterRequireNonNull`アノテーションの有無を確認し、設定されていればセットアップ処理をスキップ、されていなければ`null`をそのまま引数とします。

`KUseDefaultArgument`アノテーションが設定されていたり、`KParameterRequireNonNull`アノテーションによって全ての入力がスキップされた場合、デフォルト引数が用いられます。  
ここでデフォルト引数が利用できなかった場合は実行時エラーとなります。

#### 引数の変換処理
`KMapper`は、以下の順序で変換内容のチェック及び変換処理を行います。

**1. アノテーションによる変換処理の指定の確認**
まず初めに、入力のクラスに対応する、`KConvertBy`アノテーションや`KConverter`アノテーションによって指定された変換処理が無いかを確認します。

**2. Enumへの変換可否の確認**
入力が`String`で、かつ引数が`Enum`だった場合、入力と対応する`name`を持つ`Enum`への変換を試みます。

**3. 文字列への変換可否の確認**
引数が`String`の場合、入力を`toString`します。

**4. マッパークラスを用いた変換処理**
ここまでの変換条件に合致しなかった場合、マッパークラスを用いてネストした変換処理を行います。  
このマッピング処理には、`PlainKMapper`は`PlainKMapper`を、それ以外は`BoundKMapper`を用います。

### 入力の優先度
`KMapper`では、基本的に先に入った入力可能な引数を優先します。  
例えば、以下の例では`param1`として先に`value1`が指定されているため、`"param1" to "value2"`は無視されます。

```kotlin
val mapper: KMapper<Dst> = ...

val dst = mapper.map("param1" to "value1", "param1" to "value2")
```

ただし、`KParameterRequireNonNull`アノテーションが指定された引数に対応する入力として`null`が指定された場合、その入力は無視され、後から入った引数が優先されます。
