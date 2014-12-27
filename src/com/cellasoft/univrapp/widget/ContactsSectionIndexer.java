package com.cellasoft.univrapp.widget;

import android.widget.SectionIndexer;
import com.cellasoft.univrapp.utils.Lists;

import java.util.*;

public class ContactsSectionIndexer implements SectionIndexer {

    private static String OTHER = "#";
    private static String[] mSections;

    private static int OTHER_INDEX = 0; // index of other in the mSections array

    private int[] mPositions; // store the list of starting position index
    // for
    // each section
    // e.g. A start at index 0, B start at index
    // 20,
    // C start at index 41 and so on

    private int mCount; // this is the count for total number of contacts

    // Assumption: the contacts array has been sorted
    public ContactsSectionIndexer(List<ContactItemInterface> contacts) {
        initPositions(contacts);
    }

    public ContactsSectionIndexer() {
    }

    public String getFirstLetterForIndex(String indexableItem) {
        if (indexableItem != null) {
            indexableItem = indexableItem.trim();
            if (indexableItem.length() > 0)
                return indexableItem.substring(0, 1).toUpperCase(
                        Locale.getDefault());
        }
        return "";
    }

    public String getSectionTitle(String indexableItem) {
        int sectionIndex = getSectionIndex(indexableItem);
        return mSections[sectionIndex];
    }

    // return which section this item belong to
    public int getSectionIndex(String indexableItem) {
        if (indexableItem == null) {
            return OTHER_INDEX;
        }

        indexableItem = indexableItem.trim();

        if (indexableItem.length() == 0) {
            return OTHER_INDEX;
        }

        // get the first letter
        String firstLetter = getFirstLetterForIndex(indexableItem);
        int sectionCount = mSections.length;

        for (int i = 0; i < sectionCount; i++) {
            if (mSections[i].equals(firstLetter)) {
                return i;
            }
        }

        return OTHER_INDEX;

    }

    // initialize the position index
    public void initPositions(List<ContactItemInterface> contacts) {
        Collections.sort(contacts, new ContactItemComparator());

        mCount = contacts.size();

        HashMap<String, Integer> alphaIndexer = new HashMap<String, Integer>();
        alphaIndexer.put(OTHER, OTHER_INDEX);

        // Assumption: list of items have already been sorted by the prefer
        // names
        int itemIndex = 0;

        for (ContactItemInterface contact : contacts) {
            String ch = getFirstLetterForIndex(contact.getItemForIndex());
            if (!alphaIndexer.containsKey(ch)) {
                alphaIndexer.put(ch, itemIndex);
            }
            itemIndex++;
        }

        // init order section
        Set<String> sectionLetters = alphaIndexer.keySet();
        List<String> sectionList = Lists.newArrayList(sectionLetters
                .toArray(new String[0]));
        Collections.sort(sectionList);
        mSections = new String[sectionList.size()];
        sectionList.toArray(mSections);
        sectionList.clear();

        // init position for section
        itemIndex = 0;
        mPositions = new int[mSections.length];
        Arrays.fill(mPositions, -1); // initialize everything to -1

        for (String letter : mSections) {
            mPositions[itemIndex] = alphaIndexer.get(letter);
            itemIndex++;
        }

        alphaIndexer.clear();
        alphaIndexer = null;
    }

    @Override
    public int getPositionForSection(int section) {
        if (section < 0 || section >= mSections.length) {
            return -1;
        }

        return mPositions[section];

    }

    @Override
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= mCount) {
            return -1;
        }

        int index = Arrays.binarySearch(mPositions, position);

		/*
         * Consider this example: section positions are 0, 3, 5; the supplied
		 * position is 4. The section corresponding to position 4 starts at
		 * position 3, so the expected return value is 1. Binary search will not
		 * find 4 in the array and thus will return -insertPosition-1, i.e. -3.
		 * To get from that number to the expected value of 1 we need to negate
		 * and subtract 2.
		 */
        return index >= 0 ? index : -index - 2;
    }

    @Override
    public Object[] getSections() {
        return mSections;
    }

    // if first item in section, then return the section
    // otherwise return -1
    public boolean isFirstItemInSection(int position) { // check whether this
        // item is the first
        // item in section
        return Arrays.binarySearch(mPositions, position) > -1;
    }

}
