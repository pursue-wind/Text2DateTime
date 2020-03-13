/*
 * (C) Copyright LENOVO Corp. 1984-2016 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by LENOVO, Inc., a wholly-owned subsidiary of LENOVO. These
 * materials are provided under terms of a License Agreement between LENOVO
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to LENOVO may not be removed.
 *   LENOVO is a registered trademark of LENOVO, Inc.
 *
 */
package cn.mirrorming.text2date.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeFormat {
    private String startTime;
    private String endTime;
    private List<String> times;
}

