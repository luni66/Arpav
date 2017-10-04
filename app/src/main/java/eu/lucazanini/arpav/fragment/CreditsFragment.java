package eu.lucazanini.arpav.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import eu.lucazanini.arpav.R;

public class CreditsFragment extends Fragment {

    protected @BindView(R.id.dataTitle) TextView tvDataTitle;
    protected @BindView(R.id.dataBody) TextView tvDataBody;
    protected @BindString(R.string.arpav_site) String arpavSite;
    protected @BindString(R.string.arpav_name) String arpavName;
    protected @BindString(R.string.arpav_license_site) String arpavLicenseSite;
    protected @BindString(R.string.arpav_license_name) String arpavLicenseName;
    protected @BindString(R.string.data_title) String dataTitle;
    protected @BindString(R.string.data_body) String dataBody;

    protected @BindView(R.id.iconsTitle) TextView tvIconsTitle;
    protected @BindView(R.id.iconsBody) TextView tvIconsBody;
    protected @BindString(R.string.icons_title) String iconsTitle;
    protected @BindString(R.string.icons_body) String iconsBody;
    protected @BindString(R.string.nick_name) String nickName;
    protected @BindString(R.string.nick_site) String nickSite;
    protected @BindString(R.string.nick_license_name) String nickLicenseName;
    protected @BindString(R.string.nick_license_site) String NickLicenseSite;
    protected @BindString(R.string.emilie_name) String emilieName;
    protected @BindString(R.string.emilie_site) String emilieSite;
    protected @BindString(R.string.emilie_license_name) String emilieLicenseName;
    protected @BindString(R.string.emilie_license_site) String emilieLicenseSite;

    protected @BindView(R.id.developerTitle) TextView tvDeveloperTitle;
    protected @BindView(R.id.developerBody) TextView tvDeveloperBody;
    protected @BindString(R.string.developer_title) String developerTitle;
    protected @BindString(R.string.developer_body) String developerBody;
    protected @BindString(R.string.developer_site) String developerSite;
    protected @BindString(R.string.developer_name) String developerName;
    protected @BindString(R.string.app_license_site) String appLicenseSite;
    protected @BindString(R.string.app_license_name) String appLicenseName;
    protected @BindString(R.string.repository_site) String repositorySite;
    protected @BindString(R.string.repository_name) String repositoryName;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_credits, container, false);

        unbinder = ButterKnife.bind(this, v);

        tvDataTitle.setText(dataTitle);
        tvDataBody.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableString ssDataBody = getTextWithLink(new SpannableString(dataBody), arpavName, arpavSite);
        ssDataBody = getTextWithLink(ssDataBody, arpavLicenseName, arpavLicenseSite);
        tvDataBody.setText(ssDataBody);

        tvIconsTitle.setText(iconsTitle);
        tvIconsBody.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableString ssIconsBody = getTextWithLink(new SpannableString(iconsBody), nickName, nickSite);
        ssIconsBody = getTextWithLink(ssIconsBody, nickLicenseName, nickSite);
        ssIconsBody = getTextWithLink(ssIconsBody, emilieName, emilieSite);
        ssIconsBody = getTextWithLink(ssIconsBody, emilieLicenseName, emilieLicenseSite);
        tvIconsBody.setText(ssIconsBody);


        tvDeveloperTitle.setText(developerTitle);
        tvDeveloperBody.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableString ssDeveloperBody = getTextWithLink(new SpannableString(developerBody), developerName, developerSite);
        ssDeveloperBody = getTextWithLink(ssDeveloperBody, appLicenseName, appLicenseSite);
        ssDeveloperBody = getTextWithLink(ssDeveloperBody, repositoryName, repositorySite);
        tvDeveloperBody.setText(ssDeveloperBody);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private SpannableString getTextWithLink(SpannableString text, String link, String site){
        int start = text.toString().indexOf(link);
        int end = start + link.length();
        text.setSpan(new URLSpan(site), start, end, 0);
        return text;
    }

}
