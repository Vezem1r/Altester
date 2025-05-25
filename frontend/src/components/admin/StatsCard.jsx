import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const StatsCard = ({
  title,
  count,
  icon,
  bgColor,
  textColor,
  linkTo,
  hoverColor,
}) => {
  const { t } = useTranslation();

  return (
    <div className="bg-white overflow-hidden shadow rounded-lg">
      <div className="p-5">
        <div className="flex items-center">
          <div className={`flex-shrink-0 ${bgColor} rounded-md p-3`}>
            {icon}
          </div>
          <div className="ml-5 w-0 flex-1">
            <dl>
              <dt className="text-sm font-medium text-gray-500 truncate">
                {title}
              </dt>
              <dd>
                <div className="text-lg font-medium text-gray-900">
                  {count !== undefined && count !== null
                    ? count.toLocaleString()
                    : '0'}
                </div>
              </dd>
            </dl>
          </div>
        </div>
      </div>
      <div className="bg-gray-50 px-5 py-3">
        <div className="text-sm">
          <Link
            to={linkTo}
            className={`font-medium ${textColor} ${hoverColor}`}
          >
            {t('statsCard.viewAll', 'View all')} {title.toLowerCase()}
          </Link>
        </div>
      </div>
    </div>
  );
};

export default StatsCard;
