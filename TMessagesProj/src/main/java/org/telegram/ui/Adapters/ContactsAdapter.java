/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.GreySectionCell;
import org.telegram.ui.Cells.LetterSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.UserCell;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactsAdapter extends BaseSectionsAdapter {

    private Context mContext;
    private int onlyUsers;
    private boolean needPhonebook;
    private HashMap<Integer, TLRPC.User> ignoreUsers;
    private HashMap<Integer, ?> checkedMap;
    private boolean scrolling;
    private boolean isAdmin;

    public ContactsAdapter(Context context, int onlyUsersType, boolean arg2, HashMap<Integer, TLRPC.User> arg3, boolean arg4) {
        mContext = context;
        onlyUsers = onlyUsersType;
        needPhonebook = arg2;
        ignoreUsers = arg3;
        isAdmin = arg4;
    }

    public void setCheckedMap(HashMap<Integer, ?> map) {
        checkedMap = map;
    }

    public void setIsScrolling(boolean value) {
        scrolling = value;
    }

    @Override
    public Object getItem(int section, int position) {
        HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;

        if (onlyUsers != 0 && !isAdmin) {
            if (section < sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section));
                if (position < arr.size()) {
                    return MessagesController.getInstance().getUser(arr.get(position).user_id);
                }
            }
            return null;
        } else {
            if (section == 0) {
                return null;
            } else {
                if (section - 1 < sortedUsersSectionsArray.size()) {
                    ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1));
                    if (position < arr.size()) {
                        return MessagesController.getInstance().getUser(arr.get(position).user_id);
                    }
                    return null;
                }
            }
        }
        if (needPhonebook) {
            return ContactsController.getInstance().phoneBookContacts.get(position);
        }
        return null;
    }

    @Override
    public boolean isRowEnabled(int section, int row) {
        HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;

        if (onlyUsers != 0 && !isAdmin) {
            ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section));
            return row < arr.size();
        } else {
            if (section == 0) {
                if (needPhonebook || isAdmin) {
                    if (row == 1) {
                        return false;
                    }
                } else {
                    if (row == 3) {
                        return false;
                    }
                }
                return true;
            } else if (section - 1 < sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1));
                return row < arr.size();
            }
        }
        return true;
    }

    @Override
    public int getSectionCount() {
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;
        int count = sortedUsersSectionsArray.size();
        if (onlyUsers == 0) {
            count++;
        }
        if (isAdmin) {
            count++;
        }
        if (needPhonebook) {
            count++;
        }
        return count;
    }

    @Override
    public int getCountForSection(int section) {
        HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;

        if (onlyUsers != 0 && !isAdmin) {
            if (section < sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section));
                int count = arr.size();
                if (section != (sortedUsersSectionsArray.size() - 1) || needPhonebook) {
                    count++;
                }
                return count;
            }
        } else {
            if (section == 0) {
                if (needPhonebook || isAdmin) {
                    return 2;
                } else {
                    return 4;
                }
            } else if (section - 1 < sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1));
                int count = arr.size();
                if (section - 1 != (sortedUsersSectionsArray.size() - 1) || needPhonebook) {
                    count++;
                }
                return count;
            }
        }
        if (needPhonebook) {
            return ContactsController.getInstance().phoneBookContacts.size();
        }
        return 0;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;

        if (convertView == null) {
            convertView = new LetterSectionCell(mContext);
        }
        if (onlyUsers != 0 && !isAdmin) {
            if (section < sortedUsersSectionsArray.size()) {
                ((LetterSectionCell) convertView).setLetter(sortedUsersSectionsArray.get(section));
            } else {
                ((LetterSectionCell) convertView).setLetter("");
            }
        } else {
            if (section == 0) {
                ((LetterSectionCell) convertView).setLetter("");
            } else if (section - 1 < sortedUsersSectionsArray.size()) {
                ((LetterSectionCell) convertView).setLetter(sortedUsersSectionsArray.get(section - 1));
            } else {
                ((LetterSectionCell) convertView).setLetter("");
            }
        }
        return convertView;
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(section, position);
        if (type == 4) {
            if (convertView == null) {
                convertView = new DividerCell(mContext);
                convertView.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 28 : 72), 0, AndroidUtilities.dp(LocaleController.isRTL ? 72 : 28), 0);
            }
        } else if (type == 3) {
            if (convertView == null) {
                convertView = new GreySectionCell(mContext);
                ((GreySectionCell) convertView).setText(LocaleController.getString("Contacts", R.string.Contacts).toUpperCase());
            }
        } else if (type == 2) {
            if (convertView == null) {
                convertView = new TextCell(mContext);
            }
            TextCell actionCell = (TextCell) convertView;
            if (needPhonebook) {
                actionCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), mContext.getResources().getDrawable(R.drawable.menu_telegram));
            } else if (isAdmin) {
                actionCell.setTextAndIcon(LocaleController.getString("InviteToGroupByLink", R.string.InviteToGroupByLink), new IconicsDrawable(mContext, FontAwesome.Icon.faw_link).sizePx(48).color(0xff00a797));
            } else {
                if (position == 0) {
                    actionCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), new IconicsDrawable(mContext, FontAwesome.Icon.faw_users).sizePx(48).color(0xff0c85e6));
                } else if (position == 1) {
                    actionCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), new IconicsDrawable(mContext, FontAwesome.Icon.faw_user_secret).sizePx(48).color(0xffffc107));
                } else if (position == 2) {
                    actionCell.setTextAndIcon(LocaleController.getString("NewChannel", R.string.NewChannel), new IconicsDrawable(mContext, FontAwesome.Icon.faw_bullhorn).sizePx(48).color(0xff832194));
                }
            }
        } else if (type == 1) {
            if (convertView == null) {
                convertView = new TextCell(mContext);
            }
            ContactsController.Contact contact = ContactsController.getInstance().phoneBookContacts.get(position);
            if (contact.first_name != null && contact.last_name != null) {
                ((TextCell) convertView).setText(contact.first_name + " " + contact.last_name);
            } else if (contact.first_name != null && contact.last_name == null) {
                ((TextCell) convertView).setText(contact.first_name);
            } else {
                ((TextCell) convertView).setText(contact.last_name);
            }
        } else if (type == 0) {
            if (convertView == null) {
                convertView = new UserCell(mContext, 58, 1, false);
                ((UserCell) convertView).setStatusColors(0xffa8a8a8, 0xff3b84c0);
            }

            HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
            ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;

            ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section - (onlyUsers != 0 && !isAdmin ? 0 : 1)));
            TLRPC.User user = MessagesController.getInstance().getUser(arr.get(position).user_id);
            ((UserCell) convertView).setData(user, null, null, 0);
            if (checkedMap != null) {
                ((UserCell) convertView).setChecked(checkedMap.containsKey(user.id), !scrolling && Build.VERSION.SDK_INT > 10);
            }
            if (ignoreUsers != null) {
                if (ignoreUsers.containsKey(user.id)) {
                    ViewProxy.setAlpha(convertView, 0.5f);
                } else {
                    ViewProxy.setAlpha(convertView, 1.0f);
                }
            }
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int section, int position) {
        HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;
        if (onlyUsers != 0 && !isAdmin) {
            ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section));
            return position < arr.size() ? 0 : 4;
        } else {
            if (section == 0) {
                if (needPhonebook || isAdmin) {
                    if (position == 1) {
                        return 3;
                    }
                } else {
                    if (position == 3) {
                        return 3;
                    }
                }
                return 2;
            } else if (section - 1 < sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1));
                return position < arr.size() ? 0 : 4;
            }
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }
}
