package me.kirimin.mitsumine.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import me.kirimin.mitsumine.R;
import me.kirimin.mitsumine.db.AccountDAO;
import me.kirimin.mitsumine.network.api.BookmarkApiAccessor;
import rx.android.events.OnTextChangeEvent;
import rx.android.observables.ViewObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RegisterBookmarkFragment extends Fragment {

    private boolean isAlreadyBookmark;

    public static RegisterBookmarkFragment newFragment(String url) {
        RegisterBookmarkFragment fragment = new RegisterBookmarkFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final String url = getArguments().getString("url");
        if (url == null) {
            throw new IllegalStateException("url is null");
        }
        final View rootView = inflater.inflate(R.layout.fragment_register_bookmark, container, false);
        final View cardView = rootView.findViewById(R.id.card_view);
        cardView.setVisibility(View.INVISIBLE);

        BookmarkApiAccessor.requestIsAlreadyBookmark(url, AccountDAO.get())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        changeBookmarkStatus(aBoolean);
                        cardView.setVisibility(View.VISIBLE);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showToastIfExistsActivity(R.string.network_error);
                    }
                });

        rootView.findViewById(R.id.RegisterBookmarkRegisterButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.setEnabled(false);
                TextView comment = (TextView) rootView.findViewById(R.id.RegisterBookmarkCommentEditText);
                BookmarkApiAccessor.requestAddBookmark(url, AccountDAO.get(), comment.getText().toString())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<JSONObject>() {
                            @Override
                            public void call(JSONObject object) {
                                if (isAlreadyBookmark) {
                                    showToastIfExistsActivity(R.string.register_bookmark_edit_success);
                                } else {
                                    showToastIfExistsActivity(R.string.register_bookmark_register_success);
                                }
                                changeBookmarkStatus(true);
                                v.setEnabled(true);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                v.setEnabled(true);
                                showToastIfExistsActivity(R.string.network_error);
                            }
                        });
            }
        });
        rootView.findViewById(R.id.RegisterBookmarkDeleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.setEnabled(false);
                BookmarkApiAccessor.requestDeleteBookmark(url, AccountDAO.get())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                changeBookmarkStatus(false);
                                showToastIfExistsActivity(R.string.register_bookmark_delete_success);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                v.setEnabled(true);
                                showToastIfExistsActivity(R.string.network_error);
                            }
                        });
            }
        });
        ViewObservable.text((TextView) rootView.findViewById(R.id.RegisterBookmarkCommentEditText))
                .map(new Func1<OnTextChangeEvent, Integer>() {
                    @Override
                    public Integer call(OnTextChangeEvent onTextChangeEvent) {
                        return onTextChangeEvent.text.length();
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        TextView commentCountText = (TextView) rootView.findViewById(R.id.RegisterBookmarkCommentCountTextView);
                        commentCountText.setText(getActivity().getString(R.string.register_bookmark_limit, integer));
                    }
                });
        return rootView;
    }

    private void changeBookmarkStatus(boolean isAlreadyBookmark) {
        if (getView() == null) return;

        this.isAlreadyBookmark = isAlreadyBookmark;
        getView().findViewById(R.id.RegisterBookmarkDeleteButton).setEnabled(isAlreadyBookmark);
        Button registerButton = (Button) getView().findViewById(R.id.RegisterBookmarkRegisterButton);
        if (isAlreadyBookmark) {
            registerButton.setText(getActivity().getString(R.string.register_bookmark_edit));
        } else {
            registerButton.setText(getActivity().getString(R.string.register_bookmark_resister));
        }
    }

    private void showToastIfExistsActivity(int messageResourceId) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), messageResourceId, Toast.LENGTH_SHORT).show();
        }
    }
}
