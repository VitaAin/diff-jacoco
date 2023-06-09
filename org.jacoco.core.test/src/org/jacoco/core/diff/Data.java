package org.jacoco.core.diff;

public class Data {

    public static final String KT_PATH_2 = "app/src/main/java/com/jacoco/demo/shortcut/MyShortcutManager.kt";
    public static final String CODE_KT_2 = "package com.jacoco.demo.shortcut\n" +
            "\n" +
            "import android.annotation.SuppressLint\n" +
            "import android.annotation.TargetApi\n" +
            "import android.app.PendingIntent\n" +
            "import android.content.Context\n" +
            "import android.content.Intent\n" +
            "import android.content.pm.PackageInfo\n" +
            "import android.content.pm.PackageManager\n" +
            "import android.content.pm.ShortcutInfo\n" +
            "import android.content.pm.ShortcutManager\n" +
            "import android.graphics.Bitmap\n" +
            "import android.graphics.drawable.Icon\n" +
            "import android.net.Uri\n" +
            "import android.os.Build\n" +
            "import android.text.TextUtils\n" +
            "import android.util.Log\n" +
            "import androidx.annotation.RequiresApi\n" +
            "import androidx.core.content.pm.ShortcutInfoCompat\n" +
            "import androidx.core.content.pm.ShortcutManagerCompat\n" +
            "import androidx.core.graphics.drawable.IconCompat\n" +
            "import com.jacoco.demo.MainActivity\n" +
            "import com.jacoco.demo.R\n" +
            "import com.jacoco.demo.utils.ImageLoadUtil\n" +
            "import com.jacoco.demo.utils.ImageLoaderListener\n" +
            "\n" +
            "\n" +
            "/**\n" +
            " * @description : ShortcutManager\n" +
            " * @date : 2021-06-17 10:19\n" +
            " * @author : Administrator\n" +
            " * @version : 1.0\n" +
            " */\n" +
            "class MyShortcutManager {\n" +
            "\n" +
            "    companion object {\n" +
            "\n" +
            "        private const val ACTION_ADD_SHORTCUT = \"com.android.launcher.action.INSTALL_SHORTCUT\"\n" +
            "        private const val DEEP_LINK =\n" +
            "            \"browser2345://openurl/common?data={\\\"type\\\": 2002,\\\"loginStatus\\\": 2,\\\"url\\\": \\\"https://www.baidu.com\\\"}\"\n" +
            "\n" +
            "        fun addShortcutBelowAndroidN(context: Context) {\n" +
            "            val addShortcutIntent = Intent(ACTION_ADD_SHORTCUT)\n" +
            "\n" +
            "            // 不允许重复创建，不是根据快捷方式的名字判断重复的\n" +
            "            addShortcutIntent.putExtra(\"duplicate\", false)\n" +
            "            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, \"MyShortcut\")\n" +
            "\n" +
            "            //图标\n" +
            "            addShortcutIntent.putExtra(\n" +
            "                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,\n" +
            "                Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_launcher_round)\n" +
            "            )\n" +
            "\n" +
            "            // 设置关联程序\n" +
            "            val launcherIntent = Intent(Intent.ACTION_VIEW, Uri.parse(DEEP_LINK))\n" +
            "//            launcherIntent.setClass(context, TestUiActivity::class.java)\n" +
            "            launcherIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK\n" +
            "            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent)\n" +
            "\n" +
            "            // 发送广播\n" +
            "            context.sendBroadcast(addShortcutIntent)\n" +
            "        }\n" +
            "\n" +
            "        fun addShortCutCompact(context: Context) {\n" +
            "            var shortCutExist26 = isShortCutExist26(context, \"TestShortcutId\")\n" +
            "            Log.d(\n" +
            "                \"VITA_MyShortcutManager\",\n" +
            "                \"addShortCutCompact: shortCutExist26 = $shortCutExist26\"\n" +
            "            )\n" +
            "            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {\n" +
            "//                val shortcutInfoIntent = Intent(context, TestUiActivity::class.java)\n" +
            "//                shortcutInfoIntent.action = Intent.ACTION_VIEW //action必须设置，不然报错\n" +
            "                val shortcutInfoIntent = Intent(Intent.ACTION_VIEW, Uri.parse(DEEP_LINK))\n" +
            "                shortcutInfoIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK\n" +
            "\n" +
            "                //当添加快捷方式的确认弹框弹出来时，将被回调\n" +
            "                var intent = Intent(context, ShortcutReceiver::class.java)\n" +
            "                intent.putExtra(\"myId\", \"12345\")\n" +
            "                val shortcutCallbackIntent = PendingIntent.getBroadcast(\n" +
            "                    context, 0,\n" +
            "                    intent, PendingIntent.FLAG_UPDATE_CURRENT\n" +
            "                )\n" +
            "\n" +
            "                ImageLoadUtil.preloadImageWithListener(context,\n" +
            "                    \"https://zhushou.2345cdn.net/zhushouimg/img/logo/182/1820766_1602839219.png\",\n" +
            "                    object : ImageLoaderListener<Bitmap> {\n" +
            "                        override fun loadError() {\n" +
            "                            val info = ShortcutInfoCompat.Builder(context, \"TestShortcutId\")\n" +
            "                                .setIcon(\n" +
            "                                    IconCompat.createWithResource(\n" +
            "                                        context,\n" +
            "                                        R.drawable.ic_launcher_round\n" +
            "                                    )\n" +
            "                                )\n" +
            "                                .setShortLabel(\"ShortLabel\")\n" +
            "                                .setIntent(shortcutInfoIntent)\n" +
            "                                .build()\n" +
            "                            var res = ShortcutManagerCompat.requestPinShortcut(\n" +
            "                                context,\n" +
            "                                info,\n" +
            "                                shortcutCallbackIntent.intentSender\n" +
            "                            )\n" +
            "                            Log.i(\"VITA_MyShortcutManager\", \"loadError: res = $res\")\n" +
            "                        }\n" +
            "\n" +
            "                        override fun loadSuccess(resource: Bitmap?) {\n" +
            "                            val info = ShortcutInfoCompat.Builder(context, \"TestShortcutId\")\n" +
            "                                .setIcon(IconCompat.createWithBitmap(resource))\n" +
            "                                .setShortLabel(\"ShortLabel\")\n" +
            "                                .setIntent(shortcutInfoIntent)\n" +
            "                                .build()\n" +
            "                            var res = ShortcutManagerCompat.requestPinShortcut(\n" +
            "                                context,\n" +
            "                                info,\n" +
            "                                shortcutCallbackIntent.intentSender\n" +
            "                            )\n" +
            "                            Log.i(\"VITA_MyShortcutManager\", \"loadSuccess: res = $res\")\n" +
            "                        }\n" +
            "                    })\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        @RequiresApi(Build.VERSION_CODES.O)\n" +
            "        @SuppressLint(\"ServiceCast\")\n" +
            "        fun addShortCut(context: Context) {\n" +
            "            val shortcutManager: ShortcutManager =\n" +
            "                context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager\n" +
            "            if (shortcutManager.isRequestPinShortcutSupported) {\n" +
            "                val shortcutInfoIntent = Intent(context, MainActivity::class.java)\n" +
            "                // action必须设置，不然报错\n" +
            "                shortcutInfoIntent.action = Intent.ACTION_VIEW\n" +
            "                val info = ShortcutInfo.Builder(context, \"The only id\")\n" +
            "                    .setIcon(Icon.createWithResource(context, R.drawable.ic_launcher))\n" +
            "                    .setShortLabel(\"ShortLabel\")\n" +
            "                    .setIntent(shortcutInfoIntent)\n" +
            "                    .build()\n" +
            "\n" +
            "                //当添加快捷方式的确认弹框弹出来时，将被回调\n" +
            "                val shortcutCallbackIntent = PendingIntent.getBroadcast(\n" +
            "                    context, 0,\n" +
            "                    Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT\n" +
            "                )\n" +
            "                shortcutManager.requestPinShortcut(info, shortcutCallbackIntent.intentSender)\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        fun createShortcut(context: Context) {\n" +
            "            try {\n" +
            "            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {\n" +
            "                addShortCutCompact(context)\n" +
            "//                addShortCut(context)\n" +
            "            } else {\n" +
            "//                addShortcutBelowAndroidN(context)\n" +
            "                AmyUtils.createShortCut(context, \"ShortLabel\")\n" +
            "            }\n" +
            "            } catch (e: Exception) {\n" +
            "                e.printStackTrace()\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        @TargetApi(26)\n" +
            "        public fun isShortCutExist26(context: Context?, id: String): Boolean {\n" +
            "            if (context == null || TextUtils.isEmpty(id)) {\n" +
            "                return false\n" +
            "            }\n" +
            "            val shortcutManager =\n" +
            "                context.getSystemService(ShortcutManager::class.java) as ShortcutManager\n" +
            "            val shortcutList = shortcutManager.pinnedShortcuts\n" +
            "            if (shortcutList.size <= 0) {\n" +
            "                return false\n" +
            "            }\n" +
            "            for (i in shortcutList.indices) {\n" +
            "                val shortcutInfo = shortcutList[i] as ShortcutInfo\n" +
            "                if (TextUtils.equals(id, shortcutInfo.id)) {\n" +
            "                    return true\n" +
            "                }\n" +
            "            }\n" +
            "            return false\n" +
            "        }\n" +
            "\n" +
            "        private fun getAuthorityFromPermission(context: Context, permission: String?): String? {\n" +
            "            if (permission == null) return null\n" +
            "            val packs: List<PackageInfo>? =\n" +
            "                context.packageManager.getInstalledPackages(PackageManager.GET_PROVIDERS)\n" +
            "            if (packs != null) {\n" +
            "                for (pack in packs) {\n" +
            "                    val providers = pack.providers\n" +
            "                    if (providers != null) {\n" +
            "                        for (provider in providers) {\n" +
            "                            if (permission == provider.readPermission) return provider.authority\n" +
            "                            if (permission == provider.writePermission) return provider.authority\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "            return null\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "private var tag = false\n" +
            "\n" +
            "fun test() {\n" +
            "    Log.i(\"TAG\", \"test: 111111111111\")\n" +
            "    Log.i(\"TAG\", \"test: 2222222222\")\n" +
            "    if (tag) {\n" +
            "        Log.i(\"TAG\", \"test: 33333\")\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "fun test2() {\n" +
            "    Log.i(\"TAG\", \"test: 44444\")\n" +
            "    Log.i(\"TAG\", \"test: 5555555\")\n" +
            "    if (tag) {\n" +
            "        Log.i(\"TAG\", \"test: 6666666\")\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "fun test3() {\n" +
            "    tag = true\n" +
            "    Test().test4()\n" +
            "}\n" +
            "\n" +
            "class Test {\n" +
            "\n" +
            "    fun test4() {\n" +
            "        Log.i(\"TAG\", \"test: 44444\")\n" +
            "        if (tag) {\n" +
            "            Log.i(\"TAG\", \"test: 5555555\")\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    fun test5() {\n" +
            "        tag = true\n" +
            "        test2()\n" +
            "    }\n" +
            "\n" +
            "}";
    public static final String KT_PATH = "app/src/main/java/com/jacoco/demo/utils/ExtendUtil.kt";
    public static final String CODE_KT = "package com.jacoco.demo.utils\n" +
            "\n" +
            "import android.content.res.Resources\n" +
            "import android.util.Log\n" +
            "import android.util.TypedValue\n" +
            "\n" +
            "private var callback = object : Callback {\n" +
            "    \n" +
            "    override fun onSuccess() {\n" +
            "        Log.d(\"TAG\", \"onSuccess: \")\n" +
            "    }\n" +
            "\n" +
            "    override fun onError() {\n" +
            "        Log.d(\"TAG\", \"onSuccess: \")\n" +
            "    }\n" +
            "\n" +
            "}\n" +
            "\n" +
            "fun Int.sp2px(): Int {\n" +
            "\n" +
            "    var callback1 = object : Callback {\n" +
            "\n" +
            "        override fun onSuccess() {\n" +
            "            Log.d(\"TAG\", \"onSuccess: \")\n" +
            "        }\n" +
            "\n" +
            "        override fun onError() {\n" +
            "            Log.d(\"TAG\", \"onSuccess: \")\n" +
            "        }\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "    val displayMetrics = Resources.getSystem().displayMetrics\n" +
            "    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), displayMetrics)\n" +
            "        .toInt()\n" +
            "}\n" +
            "\n" +
            "fun Float.sp2px(): Float {\n" +
            "    val displayMetrics = Resources.getSystem().displayMetrics\n" +
            "    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, displayMetrics)\n" +
            "}\n" +
            "\n" +
            "fun Int.dp2px(): Int {\n" +
            "    val displayMetrics = Resources.getSystem().displayMetrics\n" +
            "    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), displayMetrics)\n" +
            "        .toInt()\n" +
            "}\n" +
            "\n" +
            "fun Float.dp2px(): Float {\n" +
            "    val displayMetrics = Resources.getSystem().displayMetrics\n" +
            "    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics)\n" +
            "}";
    public static final String CODE_KT_0 = "package com.jacoco.demo.utils\n" +
            "\n" +
            "import android.content.res.Resources\n" +
            "import android.util.TypedValue\n" +
            "\n" +
            "fun Int.sp2px(): Int {\n" +
            "    val displayMetrics = Resources.getSystem().displayMetrics\n" +
            "    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), displayMetrics)\n" +
            "        .toInt()\n" +
            "}\n" +
            "\n" +
            "fun Float.sp2px(): Float {\n" +
            "    val displayMetrics = Resources.getSystem().displayMetrics\n" +
            "    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, displayMetrics)\n" +
            "}\n" +
            "\n" +
            "fun Int.dp2px(): Int {\n" +
            "    val displayMetrics = Resources.getSystem().displayMetrics\n" +
            "    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), displayMetrics)\n" +
            "        .toInt()\n" +
            "}\n" +
            "\n" +
            "fun Float.dp2px(): Float {\n" +
            "    val displayMetrics = Resources.getSystem().displayMetrics\n" +
            "    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics)\n" +
            "}";

    public static final String CODE_3 = "    private Handler mHandler = new Handler(Looper.getMainLooper()) {\n" +
            "        @Override\n" +
            "        public void handleMessage(@NonNull Message msg) {\n" +
            "            super.handleMessage(msg);\n" +
            "            Log.d(\"TestUiActivity\", \"handleMessage: 1111\");\n" +
            "        }\n" +
            "    };\n";

    public static final String CODE_1 = "  nodePoint.setNodeClickListener(new NodePickViewJava.OnNodeClickListener(){\n" +
            "    @Override public void onNodeClicked(    int position){\n" +
            "      Log.d(\"VITA_TestUiActivity\",\"onNodeClicked: \" + position);\n" +
            "    }\n" +
            "  }";

    public static final String CODE_2 = "       super.onCreate(savedInstanceState);\n" +
            "        setContentView(R.layout.activity_test_ui);\n" +
            "\n" +
            "        List<String> textList = Arrays.asList(\"0MB\", \"50MB\", \"100MB\", \"150MB\", \"200MB\");\n" +
            "        List<String> textList2 = Arrays.asList(\"24h\", \"48h\", \"72h\", \"周\");\n" +
            "        List<Integer> progressIndex = Arrays.asList(1, 2);\n" +
            "        NodePickViewJava nodePoint = findViewById(R.id.node_point_view);\n" +
            "        nodePoint.setNodeData(textList);\n" +
            "        nodePoint.setSelectedIndex(2);\n" +
            "        nodePoint.setNodeClickListener(new NodePickViewJava.OnNodeClickListener() {\n" +
            "            @Override\n" +
            "            public void onNodeClicked(int position) {\n" +
            "                Log.d(\"VITA_TestUiActivity\", \"onNodeClicked: \"\n" +
            "                        + position);\n" +
            "            }\n" +
            "        });\n" +
            "\n" +
            "        NodePickView nodePoint2 = findViewById(R.id.node_point_view_2);\n" +
            "        nodePoint2.setNodeData(textList2, 1);\n" +
            "        nodePoint2.setNodeClickListener(new NodePickView.OnNodeClickListener() {\n" +
            "            @Override\n" +
            "            public void onNodeClicked(int position) {\n" +
            "                Log.d(\"VITA_TestUiActivity\", \"onNodeClicked 2: \" + position);\n" +
            "            }\n" +
            "        });\n" +
            "\n" +
            "\n" +
            "        TextView tv_shadow = findViewById(R.id.tv_shadow);\n" +
            "//        View ll_shadow = findViewById(R.id.ll_shadow);\n" +
            "        setShadowDrawable(tv_shadow, 18, Color.parseColor(\"#1a000000\"), 18, 12, 12);\n" +
            "//        setShadowDrawable(ll_shadow, 18, Color.parseColor(\"#1a000000\"), 18, 12, 12);\n" +
            "\n" +
            "        NumberProgressBar npb = findViewById(R.id.npb);\n" +
            "        npb.setMax(100);\n" +
            "        npb.setProgress(40);";

    public static final String CODE_TestUiActivity = "package com.jacoco.demo.ui;\n" +
            "\n" +
            "import android.graphics.Color;\n" +
            "import android.os.Bundle;\n" +
            "import android.os.Handler;\n" +
            "import android.os.Looper;\n" +
            "import android.os.Message;\n" +
            "import android.util.Log;\n" +
            "import android.view.LayoutInflater;\n" +
            "import android.view.View;\n" +
            "import android.view.ViewGroup;\n" +
            "import android.widget.TextView;\n" +
            "\n" +
            "import androidx.annotation.NonNull;\n" +
            "import androidx.appcompat.app.AppCompatActivity;\n" +
            "import androidx.core.view.ViewCompat;\n" +
            "import androidx.recyclerview.widget.RecyclerView;\n" +
            "\n" +
            "import com.daimajia.numberprogressbar.NumberProgressBar;\n" +
            "import com.google.android.flexbox.FlexboxLayoutManager;\n" +
            "import com.jacoco.demo.R;\n" +
            "\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class TestUiActivity extends AppCompatActivity {\n" +
            "\n" +
            "    private List<String> mList;\n" +
            "    private FlexboxLayoutManager mFlexboxLayoutManager;\n" +
            "    \n" +
            "    private Handler mHandler = new Handler(Looper.getMainLooper()) {\n" +
            "        @Override\n" +
            "        public void handleMessage(@NonNull Message msg) {\n" +
            "            super.handleMessage(msg);\n" +
            "            Log.d(\"TestUiActivity\", \"handleMessage: 1111\");\n" +
            "        }\n" +
            "    };\n" +
            "\n" +
            "    @Override\n" +
            "    protected void onCreate(Bundle savedInstanceState) {\n" +
            "        super.onCreate(savedInstanceState);\n" +
            "        setContentView(R.layout.activity_test_ui);\n" +
            "\n" +
            "        List<String> textList = Arrays.asList(\"0MB\", \"50MB\", \"100MB\", \"150MB\", \"200MB\");\n" +
            "        List<String> textList2 = Arrays.asList(\"24h\", \"48h\", \"72h\", \"周\");\n" +
            "        List<Integer> progressIndex = Arrays.asList(1, 2);\n" +
            "        NodePickViewJava nodePoint = findViewById(R.id.node_point_view);\n" +
            "        nodePoint.setNodeData(textList);\n" +
            "        nodePoint.setSelectedIndex(2);\n" +
            "        nodePoint.setNodeClickListener(new NodePickViewJava.OnNodeClickListener() {\n" +
            "            @Override\n" +
            "            public void onNodeClicked(int position) {\n" +
            "                Log.d(\"VITA_TestUiActivity\", \"onNodeClicked: \"\n" +
            "                        + position);\n" +
            "            }\n" +
            "        });\n" +
            "\n" +
            "        NodePickView nodePoint2 = findViewById(R.id.node_point_view_2);\n" +
            "        nodePoint2.setNodeData(textList2, 1);\n" +
            "        nodePoint2.setNodeClickListener(new NodePickView.OnNodeClickListener() {\n" +
            "            @Override\n" +
            "            public void onNodeClicked(int position) {\n" +
            "                Log.d(\"VITA_TestUiActivity\", \"onNodeClicked 2: \" + position);\n" +
            "            }\n" +
            "        });\n" +
            "\n" +
            "\n" +
            "        TextView tv_shadow = findViewById(R.id.tv_shadow);\n" +
            "//        View ll_shadow = findViewById(R.id.ll_shadow);\n" +
            "        setShadowDrawable(tv_shadow, 18, Color.parseColor(\"#1a000000\"), 18, 12, 12);\n" +
            "//        setShadowDrawable(ll_shadow, 18, Color.parseColor(\"#1a000000\"), 18, 12, 12);\n" +
            "\n" +
            "        NumberProgressBar npb = findViewById(R.id.npb);\n" +
            "        npb.setMax(100);\n" +
            "        npb.setProgress(40);\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "    public static void setShadowDrawable(View view, int shapeRadius, int shadowColor, int shadowRadius, int offsetX, int offsetY) {\n" +
            "        ShadowDrawable drawable = new ShadowDrawable.Builder()\n" +
            "                .setShapeRadius(shapeRadius)\n" +
            "                .setShadowColor(shadowColor)\n" +
            "                .setShadowRadius(shadowRadius)\n" +
            "                .setOffsetX(offsetX)\n" +
            "                .setOffsetY(offsetY)\n" +
            "                .builder();\n" +
            "        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);\n" +
            "        ViewCompat.setBackground(view, drawable);\n" +
            "    }\n" +
            "\n" +
            "    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {\n" +
            "\n" +
            "        @NonNull\n" +
            "        @Override\n" +
            "        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {\n" +
            "            return new MyViewHolder(LayoutInflater.from(getBaseContext()).inflate(R.layout.layout_flex_item, null));\n" +
            "        }\n" +
            "\n" +
            "        @Override\n" +
            "        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {\n" +
            "            String text = mList.get(position);\n" +
            "            holder.tv.setText(text);\n" +
            "        }\n" +
            "\n" +
            "        @Override\n" +
            "        public int getItemCount() {\n" +
            "            return mList.size();\n" +
            "        }\n" +
            "\n" +
            "        class MyViewHolder extends RecyclerView.ViewHolder {\n" +
            "            private TextView tv;\n" +
            "\n" +
            "            public MyViewHolder(@NonNull View itemView) {\n" +
            "                super(itemView);\n" +
            "                tv = itemView.findViewById(R.id.tv_text);\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "    class SeeMoreAdapter extends RecyclerView.Adapter<SeeMoreAdapter.SeeMoreViewHolder> {\n" +
            "\n" +
            "        private final static int TYPE_NORMAL = 0;//正常条目\n" +
            "        private final static int TYPE_SEE_MORE = 1;//查看更多\n" +
            "        private final static int TYPE_HIDE = 2;//收起\n" +
            "        private boolean mOpen = false;//是否是展开状态\n" +
            "\n" +
            "        @NonNull\n" +
            "        @Override\n" +
            "        public SeeMoreViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {\n" +
            "            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_flex_item, viewGroup, false);\n" +
            "            return new SeeMoreViewHolder(view);\n" +
            "        }\n" +
            "\n" +
            "        @Override\n" +
            "        public void onBindViewHolder(@NonNull SeeMoreViewHolder seeMoreViewHolder, int position) {\n" +
            "            TextView textView = seeMoreViewHolder.tv;\n" +
            "            if (getItemViewType(position) == TYPE_HIDE) {\n" +
            "                textView.setText(\"收起\");\n" +
            "                textView.setOnClickListener(new View.OnClickListener() {\n" +
            "                    @Override\n" +
            "                    public void onClick(View v) {\n" +
            "                        mOpen = false;\n" +
            "                        notifyDataSetChanged();\n" +
            "                    }\n" +
            "                });\n" +
            "            } else if (getItemViewType(position) == TYPE_SEE_MORE) {\n" +
            "                textView.setText(\"更多\");\n" +
            "                textView.setOnClickListener(new View.OnClickListener() {\n" +
            "                    @Override\n" +
            "                    public void onClick(View v) {\n" +
            "                        mOpen = true;\n" +
            "                        notifyDataSetChanged();\n" +
            "                    }\n" +
            "                });\n" +
            "            } else {\n" +
            "                textView.setText(mList.get(position));\n" +
            "                textView.setClickable(false);\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        @Override\n" +
            "        public int getItemViewType(int position) {\n" +
            "            if (mList.size() <= 4) {\n" +
            "                return TYPE_NORMAL;\n" +
            "            }\n" +
            "            if (mOpen) {\n" +
            "                if (position == mList.size()) {\n" +
            "                    return TYPE_HIDE;\n" +
            "                } else {\n" +
            "                    return TYPE_NORMAL;\n" +
            "                }\n" +
            "            } else {\n" +
            "                if (position == 3) {\n" +
            "                    return TYPE_SEE_MORE;\n" +
            "                } else {\n" +
            "                    return TYPE_NORMAL;\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        @Override\n" +
            "        public int getItemCount() {\n" +
            "            if (mList == null || mList.size() == 0) {\n" +
            "                return 0;\n" +
            "            }\n" +
            "            if (mList.size() > 4) {\n" +
            "                //若现在是展开状态 条目数量需要+1 \"收起\"条目\n" +
            "                if (mOpen) {\n" +
            "                    return mList.size() + 1;\n" +
            "                } else {\n" +
            "                    return 4;\n" +
            "                }\n" +
            "            } else {\n" +
            "                return mList.size();\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        class SeeMoreViewHolder extends RecyclerView.ViewHolder {\n" +
            "\n" +
            "            private TextView tv;\n" +
            "\n" +
            "            public SeeMoreViewHolder(@NonNull View itemView) {\n" +
            "                super(itemView);\n" +
            "                tv = itemView.findViewById(R.id.tv_text);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

}
