package cn.edu.hebtu.software.listendemo.Host.index;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.hebtu.software.listendemo.Entity.Book;
import cn.edu.hebtu.software.listendemo.Host.bookDetail.BookDetailActivity;
import cn.edu.hebtu.software.listendemo.Host.searchBook.SearchBookActivity;
import cn.edu.hebtu.software.listendemo.R;
import cn.edu.hebtu.software.listendemo.Untils.Constant;
import cn.edu.hebtu.software.listendemo.Untils.StatusBarUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static cn.edu.hebtu.software.listendemo.Untils.Constant.BOOK_JSON;

public class HostFragment extends Fragment {
    private LinearLayout llFindBooks;
    private LinearLayout llContinueStudy;
    private List<Book> res = new ArrayList<>();
    private HostRecyclerAdapter adapter = null;
    private RecyclerView recyclerView = null;
    private SharedPreferences sp = null;
    private Gson gson = new GsonBuilder().serializeNulls().create();
    private View view;
    private static final int GET_BOOKS = 1;
    @BindView(R.id.tv_continue)
    TextView tvContinue;
    private Banner banner;

    private List<String> mTitleList = new ArrayList<>();
    private List<Integer> mImgList = new ArrayList<>();


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_BOOKS:
                    initView(view);
                    setListener();
                    break;
            }
        }
    };
    private OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_host, container, false);
        initData();
        ButterKnife.bind(this,view);
        banner = view.findViewById(R.id.play_banner);
        // 设置轮播图
        BannerSet();
        return view;
    }

    private void setListener() {
        llFindBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchBookActivity.class);
                startActivity(intent);
            }
        });
        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getContext().getSharedPreferences(Constant.SP_NAME, MODE_PRIVATE);
                String bookJson = sp.getString(BOOK_JSON,"");
                if("".equals(bookJson)){
                    Toast.makeText(getContext(),"没有找到当前的学习记录",Toast.LENGTH_SHORT).show();
                }else{
                    Book book = gson.fromJson(bookJson,Book.class);
                    Intent intent = new Intent(getActivity(), BookDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constant.HOST_CON_DETAIL_BOOK,book);
                    intent.putExtras(bundle);
                    getContext().startActivity(intent);
                }
            }
        });
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.recv_fragment_host);
        adapter = new HostRecyclerAdapter(R.layout.fragment_host_recycler_item, res, getContext());
        recyclerView.setAdapter(adapter);
        llContinueStudy = view.findViewById(R.id.ll_fragment_host_continueStudy);
        llFindBooks = view.findViewById(R.id.ll_fragment_host_findBooks);
    }

    private void initData() {
        sp = getContext().getSharedPreferences(Constant.SP_NAME, MODE_PRIVATE);
        FormBody fb = new FormBody.Builder().build();
        Request request = new Request.Builder().url(Constant.URL_BOOKS_FIND_ALL).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonBooks = response.body().string();
                Message message = new Message();
                message.what = GET_BOOKS;
                Type type = new TypeToken<List<Book>>() {
                }.getType();
                res = gson.fromJson(jsonBooks, type);
                handler.sendMessage(message);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBarColor(getActivity(),R.color.white);
    }

    @Override
    public void onResume() {
        super.onResume();
        initView(view);
        StatusBarUtil.setStatusBarColor(getActivity(),R.color.white);
    }

    private void BannerSet() {
        mImgList.clear();
        mImgList.add(R.drawable.banner1);
        mImgList.add(R.drawable.banner2);
        mTitleList.clear();
        for (int i = 0; i < mImgList.size(); i++) {
            mTitleList.add("第" + i + "张图片");
        }

        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE); // 显示圆形指示器和标题（水平显示
        //设置图片加载器
        banner.setImageLoader(new MyLoader());
        //设置图片集合
        banner.setImages(mImgList);
        //设置banner动画效果
        banner.setBannerAnimation(Transformer.DepthPage);
        //设置标题集合（当banner样式有显示title时）
        banner.setBannerTitles(mTitleList);
        //设置自动轮播，默认为true
        banner.isAutoPlay(true);
        //设置轮播时间
        banner.setDelayTime(1500);
        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.CENTER);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
        // setOnBannerClickListener  1.4.9 以后就废弃了 。  setOnBannerListener 是1.4.9以后使用。
        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                Log.e("tt","第" + position + "张轮播图点击了！");
                //Toast.makeText(this,"",Toast.LENGTH_LONG).show();
                //UIUtils.showToast("第" + position + "张轮播图点击了！");
            }
        });


    }

    // 图片加载器
    public class MyLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context).load(path).into(imageView);
        }

    }


}
