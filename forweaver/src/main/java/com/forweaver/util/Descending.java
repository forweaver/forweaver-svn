package com.forweaver.util;

import java.util.Comparator;
import java.util.Date;

import com.forweaver.domain.vc.VCSimpleCommitLog;

public class Descending implements Comparator<VCSimpleCommitLog>{

	public int compare(VCSimpleCommitLog revesion_1, VCSimpleCommitLog revesion_2) {
		Integer revesion1 = Integer.parseInt(revesion_1.getCommitLogID());
		Integer revesion2 = Integer.parseInt(revesion_2.getCommitLogID());
		
		//내림차순이 될려면 뒤에거에서 앞에거를 비교해야 한다.//
		//오름차순은 반대로 앞에거에서 뒤에거를 비교해야 한다.//
		//compareTo()는 사전식으로 비교한다.//
		return revesion2.compareTo(revesion1);
	}
}
