/*
 *
 *  Copyright 2016 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.hollow.diffview;


public abstract class HollowObjectView {

    private static final int MAX_INITIAL_VISIBLE_ROWS_BEFORE_COLLAPSING_DIFFS = 300;

    private final HollowDiffViewRow rootRow;

    public HollowObjectView(HollowDiffViewRow rootRow) {
        this.rootRow = rootRow;
    }

    public HollowDiffViewRow getRootRow() {
        return rootRow;
    }

    public void resetView() {
        int totalVisibleRows = resetViewForDiff(rootRow, 0);
        
        if(totalVisibleRows > MAX_INITIAL_VISIBLE_ROWS_BEFORE_COLLAPSING_DIFFS) {
            collapseChildrenUnderRootDiffRows(rootRow);
        } else if(totalVisibleRows == 0) {
            totalVisibleRows = resetViewForOrderingChanges(rootRow, 0);
            if(totalVisibleRows > MAX_INITIAL_VISIBLE_ROWS_BEFORE_COLLAPSING_DIFFS) {
                collapseChildrenUnderRootOrderingDiffRows(rootRow);
            }
        }
    }
    
    private int resetViewForDiff(HollowDiffViewRow row, int runningVisibilityCount) {
        int branchVisibilityCount = 0;
        
        if(row.getFieldPair().isDiff()) {
            row.setVisibility(true);
            branchVisibilityCount++;
            
            branchVisibilityCount += makeAllChildrenVisible(row, branchVisibilityCount + runningVisibilityCount);
        } else {
            for(HollowDiffViewRow child : row.getChildren()) {
                branchVisibilityCount += resetViewForDiff(child, branchVisibilityCount + runningVisibilityCount);
                
                if(branchVisibilityCount > 0) {
                    row.setVisibility(true);
                    branchVisibilityCount++;
                }
            }
        }
        
        return branchVisibilityCount;
    }
    
    private int makeAllChildrenVisible(HollowDiffViewRow row, int runningVisibilityCount) {
        int branchVisibilityCount = 0;
        
        for(HollowDiffViewRow child : row.getChildren()) {
            child.setVisibility(true);
            branchVisibilityCount++;
            
            branchVisibilityCount += makeAllChildrenVisible(child, branchVisibilityCount);
        }
        
        return branchVisibilityCount;
    }
    
    private void collapseChildrenUnderRootDiffRows(HollowDiffViewRow row) {
        for(HollowDiffViewRow child : row.getChildren()) {
            if(child.getFieldPair().isDiff()) {
                makeAllChildrenInvisible(child);
            } else {
                collapseChildrenUnderRootDiffRows(child);
            }
        }
    }

    private int resetViewForOrderingChanges(HollowDiffViewRow row, int runningVisibilityCount) {
        int branchVisibilityCount = 0;

        if(row.getFieldPair().isOrderingDiff()) {
            row.setVisibility(true);
            branchVisibilityCount++;
        } else {
            for(HollowDiffViewRow child : row.getChildren()) {
                int childBranchVisibilityCount = resetViewForOrderingChanges(child, runningVisibilityCount + branchVisibilityCount);
                
                if(childBranchVisibilityCount > 0) {
                    row.setVisibility(true);
                    branchVisibilityCount += childBranchVisibilityCount;
                }
            }
        }
        
        return branchVisibilityCount;
    }
    
    private void collapseChildrenUnderRootOrderingDiffRows(HollowDiffViewRow row) {
        for(HollowDiffViewRow child : row.getChildren()) {
            if(child.getFieldPair().isOrderingDiff()) {
                makeAllChildrenInvisible(child);
            } else {
                collapseChildrenUnderRootOrderingDiffRows(child);
            }
        }
    }
    
    private void makeAllChildrenInvisible(HollowDiffViewRow row) {
        for(HollowDiffViewRow child : row.getChildren()) {
            child.setVisibility(false);
            makeAllChildrenInvisible(child);
        }
    }

}
