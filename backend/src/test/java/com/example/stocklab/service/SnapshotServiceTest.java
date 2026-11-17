package com.example.stocklab.service;

import com.example.stocklab.service.Snapshot.Tocka;
import com.example.stocklab.service.SnapshotService.CenaDne;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** #61: posnetek ne sme vsebovati podatkov po odločitvenem datumu, ne imen in ne datumov v promptu. */
class SnapshotServiceTest {

    static final LocalDate OD = LocalDate.of(2017, 1, 2);
    static final LocalDate ODL = LocalDate.of(2018, 1, 2);

    /** Dnevne cene od..do (pon-pet), cena raste za 1 na dan. */
    static List<CenaDne> dnevne(LocalDate od, LocalDate doDatuma) {
        List<CenaDne> cene = new ArrayList<>();
        double cena = 50;
        for (LocalDate d = od; !d.isAfter(doDatuma); d = d.plusDays(1)) {
            if (d.getDayOfWeek().getValue() <= 5) {
                cene.add(new CenaDne(d, BigDecimal.valueOf(cena++)));
            }
        }
        return cene;
    }

    @Test
    void niPodatkovPoOdlocitvenemDatumu() {
        // vhod namenoma sega dve leti v prihodnost
        List<Tocka> tocke = SnapshotService.pripravi(dnevne(OD, ODL.plusYears(2)), OD, ODL);

        assertThat(tocke).isNotEmpty();
        assertThat(tocke).allSatisfy(t -> assertThat(t.datum()).isBeforeOrEqualTo(ODL));
    }

    @Test
    void niPodatkovPredZacetkomZgodovine() {
        List<Tocka> tocke = SnapshotService.pripravi(dnevne(OD.minusYears(3), ODL), OD, ODL);
        assertThat(tocke).allSatisfy(t -> assertThat(t.datum()).isAfterOrEqualTo(OD));
    }

    @Test
    void prvaVrednostJe100() {
        List<Tocka> tocke = SnapshotService.pripravi(dnevne(OD, ODL), OD, ODL);
        assertThat(tocke.get(0).vrednost()).isEqualTo(100.0);
    }

    @Test
    void enaTockaNaTeden_zadnjiDanVTednu() {
        // pon 2018-01-01 .. pet 2018-01-19 = 3 tedni
        List<Tocka> tocke = SnapshotService.pripravi(
                dnevne(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 19)),
                LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 19));

        assertThat(tocke).hasSize(3);
        assertThat(tocke).extracting(Tocka::datum).containsExactly(
                LocalDate.of(2018, 1, 5), LocalDate.of(2018, 1, 12), LocalDate.of(2018, 1, 19));
    }

    @Test
    void promptVsebujeOznakeInNobenihDatumov() {
        List<Tocka> tocke = SnapshotService.pripravi(dnevne(OD, ODL), OD, ODL);
        Snapshot s = new Snapshot(1, 60, List.of(
                SnapshotService.statistika(7L, "S042", tocke),
                SnapshotService.statistika(8L, "S117", tocke)));

        String prompt = PromptBuilder.uporabnisko(s);

        assertThat(prompt).contains("S042", "S117");
        assertThat(prompt).doesNotContainPattern("\\d{4}-\\d{2}-\\d{2}");   // brez koledarskih datumov
        assertThat(s.zadnjiDatum()).isBeforeOrEqualTo(ODL);
        assertThat(s.stockIdPoOznaki()).containsEntry("S042", 7L);
    }
}
