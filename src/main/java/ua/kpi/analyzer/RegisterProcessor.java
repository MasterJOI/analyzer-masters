package ua.kpi.analyzer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.kpi.analyzer.requests.HttpRequestBrowser;
import ua.kpi.analyzer.things.Publication;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Ihor Sytnik
 */
@Component
public class RegisterProcessor {

    @Autowired
    private HttpRequestBrowser registerRequestBrowser;

    private String getPageByISSN(String ISSN) throws InterruptedException {
        return registerRequestBrowser
                .get(
                        "/search?issnSearch=%s"
                                .formatted(
                                        ISSN
                                )
                ).bodyToMono(String.class).block();
    }

    private Set<String> getSpecialties(String html) {
        Set<String> specialties = new HashSet<>();
        Document document = Jsoup.parse(html);

        for (var specialtiesHTML :
                document.getElementsByAttributeValueMatching(
                        "href", Pattern.compile("/search\\?specialnistSearch\\[]=\\d{3}"))) {
            specialties.add(
                    specialtiesHTML.getElementsByTag("span").first().text()
            );
        }

        return specialties;
    }

    private Set<String> getSpecialtiesByISSN(String ISSN) throws InterruptedException {
        return getSpecialties(getPageByISSN(ISSN));
    }

    public Set<String> getSpecialtiesFromRegister(Publication publication) throws InterruptedException {
        Set<String> specialties = new HashSet<>();
        String html;

        if (publication.getIssn() != null) {
            specialties.addAll(getSpecialtiesByISSN(publication.getIssn()));
        }
        if (publication.getEissn() != null) {
            specialties.addAll(getSpecialtiesByISSN(publication.getEissn()));
        }

        return specialties;
    }

}
